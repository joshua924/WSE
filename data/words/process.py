f = open("count_2w.txt", "r")
g = open("two_word_phrase", "w")

li = []
for each in f.readlines():
    tmp = each.split("\t")
    ele = (tmp[0], tmp[1])
    li.append(ele)
f.close()

li.sort(key=lambda x : x[1])
li.reverse()

for (a,b) in li:
    g.write(a + "\n")
g.close()
