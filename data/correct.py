import csv


year = '2019'
month = '04'
file = open('2019/questions_{0}-{1}.csv'.format(year, month), 'r', encoding='utf-8', newline='')
csv_reader = csv.reader(file, delimiter=',', quotechar='"')
temp = []
new_file = open('2019/questions_{0}-{1}_new.csv'.format(year, month), 'w', encoding='utf-8', newline='')
new_csv_writer = csv.writer(new_file, delimiter=',', quotechar='"')
new_csv_writer.writerow(['questionID', 'questionName', 'askedTime', 'votes', 'answers', 'views', 'tags'])
for row in csv_reader:
	if row[2] >= "{0}-05-01 00:00:00Z".format(year):
		temp.append(row)
	else:
		new_csv_writer.writerow(row)
file.close()
new_file.close()
# next_file = open('2022/questions_{0}-11.csv'.format(year, month), 'a+', encoding='utf-8', newline='')
# csv_writer = csv.writer(next_file, delimiter=',', quotechar='"')
# for i in temp:
	# csv_writer.writerow(i)
# next_file.close()




