FROM postgres:9.5.4

ENV POSTGRES_USER=scott
ENV POSTGRES_PASSWORD=tiger
ENV POSTGRES_DB=pgsqlTightBlog
COPY postgresql.sql /docker-entrypoint-initdb.d/
