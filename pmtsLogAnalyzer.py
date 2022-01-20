#! /usr/bin/env python3
import sqlite3
import sys
import os

def readPackge(reader):
    content = []
    # read to the heading
    line = reader.readline().strip()
    while not "Level 0 PMTSMSGHDL" in line:
        line = reader.readline().strip()
        if not line:
            return []
    content.append(line)

    # read the rest content
    for line in reader:
        content.append(line.strip())
        if len(content) > 4 and content[-1] == "" and content[-2] == "" and content[-3] == "" and content[-4] == "":
            break
    # print(content[:-4])
    return content[:-4]

def analysisPackge(package):
    pmts_time = package[0][1:23]
    last_node_time = package[1][9:31]
    mq_name = package[2][9:17]
    mqsq_name = package[2][25:34]
    mqtq_name = package[3][9:21]
    msgtype = ""
    msgid = ""
    orgnl_msgid = ""
    for line in package[4:]:
        if line.startswith("{H:"):
            index = line.find("XML")
            msgtype = line[index+3:index+18]
        elif "<MsgId>" in line:
            start=line.find("<MsgId>")+len("<MsgId>")
            end=line.find("</MsgId>")
            msgid = line[start:end]
        elif "<OrgnlMsgId>" in line:
            start=line.find("<OrgnlMsgId>")+len("<OrgnlMsgId>")
            end=line.find("</OrgnlMsgId>")
            orgnl_msgid = line[start:end]
    return {'pmts_time': pmts_time,
            'last_node_time': last_node_time,
            'mq_name': mq_name,
            'mqsq_name': mqsq_name,
            'mqtq_name': mqtq_name,
            'msgtype': msgtype,
            'msgid': msgid,
            'orgnl_msgid': orgnl_msgid}

def savePackage(package,cursor):
    try:
        sql_template = "insert into PMTSLOGDATA(msgid, msgtype,orgnl_msgid, pmts_time, last_node_time, mq_name, mqsq_name, mqtq_name) values (:msgid,:msgtype,:orgnl_msgid,:pmts_time,:last_node_time,:mq_name,:mqsq_name,:mqtq_name)"
        cursor.execute(sql_template, package)
        print(package)
    except sqlite3.IntegrityError:
        print("Save package error:", package)


def pmtsFile2DB(pmts_file, cursor):
    with open(pmtsLogFile, "r", encoding="UTF-8") as pmtsLogFileReader:
        pkg = readPackge(pmtsLogFileReader)
        while pkg:
            savePackage(analysisPackge(pkg),cursor)
            pkg = readPackge(pmtsLogFileReader)



db_file = "pmts.db"
db_is_new = not os.path.exists(db_file)
with sqlite3.connect(db_file) as db_conn:
    if db_is_new:
        print("Creating db schema")
        sql = '''create table PMTSLOGDATA(
        msgid  varchar(50) primary key,
        msgtype varchar(50),
        orgnl_msgid  varchar(50),
        pmts_time timestamp,
        last_node_time timestamp,
        mq_name varchar(50),
        mqsq_name varchar(50),
        mqtq_name varchar(50))
        '''
        db_conn.executescript(sql)

    cursor = db_conn.cursor()

    for pmtsLogFile in sys.argv[1:]:
        pmtsFile2DB(pmtsLogFile, cursor)
