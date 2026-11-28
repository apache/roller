# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Tiered LLM router for Apache Roller security triage.

Supports:
- Tier 1 (CHEAP): routine extraction, email drafting, air-gapped public text drafting.
- Tier 2 (FRONTIER): deep codebase investigation, severity/CVSS assessment, disclosure audit.
- Privacy enforcement: when ``local_llm_only=True`` and ``private_context=True``,
  routes strictly to a local ``ollama:`` model and rejects any cloud provider spec.
"""

from __future__ import annotations

import os
from enum import Enum
from typing import Any, Callable, TypeVar

from pydantic import BaseModel

T = TypeVar("T", bound=BaseModel)


class ModelTier(str, Enum):
    CHEAP = "cheap"
    FRONTIER = "frontier"


DEFAULT_MODELS: dict[ModelTier, str] = {
    ModelTier.CHEAP: "anthropic:claude-sonnet-5-5",
    ModelTier.FRONTIER: "anthropic:claude-opus-5-5",
}

DEFAULT_LOCAL_MODEL = "ollama:qwen2.5:14b"

_ENV_VAR_BY_TIER: dict[ModelTier, str] = {
    ModelTier.CHEAP: "ROLLER_SEC_CHEAP_MODEL",
    ModelTier.FRONTIER: "ROLLER_SEC_FRONTIER_MODEL",
}


def resolve_model_spec(
    tier: ModelTier,
    *,
    local_llm_only: bool = False,
    private_context: bool = True,
) -> str:
    """Resolve the ``provider:model`` spec for a given tier and privacy context.

    When ``local_llm_only`` and ``private_context`` are both True, returns
    ``ROLLER_SEC_LOCAL_MODEL`` (defaulting to ``DEFAULT_LOCAL_MODEL``) and
    verifies that it uses the ``ollama:`` provider.
    """
    if local_llm_only and private_context:
        spec = (os.environ.get("ROLLER_SEC_LOCAL_MODEL") or DEFAULT_LOCAL_MODEL).strip()
        if not spec.startswith("ollama:") or not spec.split(":", 1)[1].strip():
            raise ValueError(
                f"local_llm_only=True with private_context=True requires an 'ollama:<model>' spec, "
                f"got {spec!r}"
            )
        return spec

    env_var = _ENV_VAR_BY_TIER[tier]
    spec = (os.environ.get(env_var) or DEFAULT_MODELS[tier]).strip()
    if ":" not in spec or not spec.split(":", 1)[1].strip():
        raise ValueError(f"Invalid model spec {spec!r}; expected '<provider>:<model>'")
    return spec


def build_chat_model(spec: str, *, temperature: float = 0.0) -> Any:
    """Instantiate a LangChain chat model from a ``provider:model`` spec."""
    if ":" not in spec:
        raise ValueError(f"Invalid model spec {spec!r}; expected '<provider>:<model>'")
    provider, model_name = spec.split(":", 1)
    provider = provider.strip().lower()
    model_name = model_name.strip()
    if not provider or not model_name:
        raise ValueError(f"Invalid model spec {spec!r}; expected '<provider>:<model>'")

    if provider == "google":
        try:
            from langchain_google_genai import ChatGoogleGenerativeAI
        except ImportError as exc:
            raise ImportError(
                "Provider 'google' requires 'langchain-google-genai'. "
                "Install with: pip install 'roller-security-graph[google]'"
            ) from exc
        return ChatGoogleGenerativeAI(model=model_name, temperature=temperature)

    if provider == "anthropic":
        try:
            from langchain_anthropic import ChatAnthropic
        except ImportError as exc:
            raise ImportError(
                "Provider 'anthropic' requires 'langchain-anthropic'. "
                "Install with: pip install 'roller-security-graph[anthropic]'"
            ) from exc
        return ChatAnthropic(model=model_name, temperature=temperature)

    if provider == "openai":
        try:
            from langchain_openai import ChatOpenAI
        except ImportError as exc:
            raise ImportError(
                "Provider 'openai' requires 'langchain-openai'. "
                "Install with: pip install 'roller-security-graph[openai]'"
            ) from exc
        return ChatOpenAI(model=model_name, temperature=temperature)

    if provider == "ollama":
        try:
            from langchain_ollama import ChatOllama
        except ImportError as exc:
            raise ImportError(
                "Provider 'ollama' requires 'langchain-ollama'. "
                "Install with: pip install 'roller-security-graph[ollama]'"
            ) from exc
        return ChatOllama(model=model_name, temperature=temperature)

    raise ValueError(
        f"Unsupported model provider {provider!r} in spec {spec!r}. "
        "Supported providers: google, anthropic, openai, ollama."
    )


def invoke_structured(
    tier: ModelTier,
    schema: type[T],
    prompt: str,
    *,
    mock_llm: bool,
    mock_factory: Callable[[], T],
    local_llm_only: bool = False,
    private_context: bool = True,
) -> tuple[T, str]:
    """Invoke a tiered LLM for a Pydantic structured output (or deterministic mock).

    Returns ``(result, provenance)`` where ``provenance`` records the tier and
    resolved model spec for the case audit history.
    """
    spec = resolve_model_spec(
        tier,
        local_llm_only=local_llm_only,
        private_context=private_context,
    )
    if mock_llm:
        return mock_factory(), f"mock:{tier.value} ({spec})"

    llm = build_chat_model(spec)
    structured_llm = llm.with_structured_output(schema)
    result: T = structured_llm.invoke(prompt)
    return result, f"{tier.value}:{spec}"
