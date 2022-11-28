from requests_html import HTMLSession
import csv
import time


def flush(buffer, year, month):
	file = open('questions_{0}-{1}.csv'.format(year, month), 'a+', encoding='utf-8', newline='')
	csv_writer = csv.writer(file, delimiter=',', quotechar='"')
	for row in buffer:
		csv_writer.writerow(row)
	file.close()


def create(year, month):
	file = open('questions_{0}-{1}.csv'.format(year, month), 'w', encoding='utf-8', newline='')
	csv_writer = csv.writer(file, delimiter=',', quotechar='"')
	csv_writer.writerow(['questionID', 'questionName', 'askedTime', 'votes', 'answers', 'views', 'tags'])
	file.close()


def generateNextMonth(month):
	months = ['12', '11', '10', '09', '08', '07', '06', '05', '04', '03', '02', '01']
	m = months.index(month)
	if m == 11:
		return months[0]
	else:
		return months[m+1]


def generateNextYear(year, month):
	# years = ['2022', '2021', '2020', '2019', '2018']
	years = ['2021']
	months = ['12', '11', '10', '09', '08', '07', '06', '05', '04', '03', '02', '01']
	y = years.index(year)
	m = months.index(month)
	if m == 11:
		return years[y+1]
	else:
		return year


year = "2021"
month = "01"
session = HTMLSession()
buffer = []
endFlag = False
pageEndQuestionID = '65985542'
# 2021-12-31 page>30500
# 2021-01-01 page<63000
for page in range(59668, 63000):
	if endFlag:
		flush(buffer, year, month)
		buffer.clear()
		break
	if page % 200 == 0:
		flush(buffer, year, month)
		buffer.clear()
		time.sleep(300)
	url = 'https://stackoverflow.com/questions?tab=newest&pagesize=50&page={0}'
	r = session.get(url.format(page))
	print(page)
	for i in range(1, 51):
		temp = []
		questionIDSelector = "#questions> div:nth-child({0})"
		try:
			questionID = r.html.find(questionIDSelector.format(i))[0].attrs.get("data-post-id")
			if questionID > pageEndQuestionID:
				continue
			temp.append(questionID)
			print(questionID)
			if i == 50:
				pageEndQuestionID = questionID
		except IndexError:
			continue
		questionSelector = '#questions> div:nth-child({0}) > div.s-post-summary--content > h3 > a'
		question = r.html.find(questionSelector.format(i))[0].text
		temp.append(question)
		print(question)
		try:
			timeStampSelector = '#questions> div:nth-child({0}) > div.s-post-summary--content > div.s-post-summary--meta > div.s-user-card.s-user-card__minimal > time > span'
			timeStamp = r.html.find(timeStampSelector.format(i))[0].attrs.get("title")
		except IndexError:
			continue
		if timeStamp <= "{0}-{1}-01 00:00:00Z".format(year, month):
			flush(buffer, year, month)
			buffer.clear()
			try:
				year = generateNextYear(year, month)
				month = generateNextMonth(month)
				create(year, month)
			except IndexError:
				print("end")
				endFlag = True
				break
		if timeStamp > "{0}-{1}-31 23:59:59Z".format(year, month):
			continue
		temp.append(timeStamp)
		print(timeStamp)
		voteSelector = "#questions > div:nth-child({0}) > div.s-post-summary--stats.js-post-summary-stats > div:nth-child(1) > span.s-post-summary--stats-item-number"
		votes = r.html.find(voteSelector.format(i))[0].text
		temp.append(votes)
		answerSelector = "#questions > div:nth-child({0}) > div.s-post-summary--stats.js-post-summary-stats > div:nth-child(2) > span.s-post-summary--stats-item-number"
		answers = r.html.find(answerSelector.format(i))[0].text
		temp.append(answers)
		viewSelector = "#questions > div:nth-child({0}) > div.s-post-summary--stats.js-post-summary-stats > div:nth-child(3) > span.s-post-summary--stats-item-number"
		views = r.html.find(viewSelector.format(i))[0].text
		temp.append(views)
		questionTagsSelector = '#questions> div:nth-child({0}) > div.s-post-summary--content > div.s-post-summary--meta > div:nth-child(1) > ul '
		tagNames = r.html.find(questionTagsSelector.format(i))[0].text
		print(tagNames)
		temp.append(tagNames.replace('\n', ';'))
		print(temp)
		buffer.append(temp)
