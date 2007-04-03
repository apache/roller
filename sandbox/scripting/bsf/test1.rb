
require 'java'

puts "--------------------------------------------"
puts "Hello"

puts "--------------------------------------------"
a = java.util.ArrayList.new

a.add("foo");
a.add("bar");
a.add("baz");

puts a

for i in a
   puts i
end


puts "--------------------------------------------"
h = java.util.HashMap.new
h.put("foo",10)

h.each {|x| puts x}

puts h.keySet()
