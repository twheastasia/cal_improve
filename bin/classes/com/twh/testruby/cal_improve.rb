total = 37800

arr = [
  [9648, 3],
  [9600, 2],
  [8440, 0],
  [6024, 1],
  [5328, 1],
  [4816, 3],
  [4660, 0],
  [2448, 3],
  [400, 3]
]

RESULT = [] 

def try_next_recursion(arr, total, index, last_result=[])
  for t in (0..([total/arr[index][0]+1,arr[index][1]].min))
    tmp_result = last_result.dup 

    if index == arr.length - 1 
	    tmp_result.unshift(-(total-arr[index][0]*t)) 
		RESULT << tmp_result
	 	next
    else 
      try_next_recursion arr, total-arr[index][0]*t, index+1, tmp_result
    end
  end

end


def abs(x)
  x > 0 ? x : -x
end

is_first = true
arr.each_with_index do |value, index|
  next if 0 == value[1] 
  str += ", " unless is_first
  is_first = false
  str += "%5d * %2d" % [value[0], value[1]]
puts str

puts "total: #{total}"

RESULT.select{|x| x[0] >= 0 && x[0] < 400}.sort{|x,y| abs(x[0]) <=> abs(y[0])}.first(10).each do |x|
  str = "%3d wasted: " % x[0]
  is_first = true
  x.each_with_index do |value, index|
    next if 0 == value || 0 == index
    str += ' + ' unless is_first
    is_first = false
    str += "%5d * %2d" % [arr[index-1][0], value]
  end
  puts str
end
