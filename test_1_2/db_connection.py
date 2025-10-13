import sqlite3
import threading
import time
import random

class DatabaseConnector:
    def __init__(self):
        self.connection_str = ":memory:"

    def get_connection(self):
        return sqlite3.connect(self.connection_str)

class DataHandler:
    def __init__(self, db):
        self.db = db

    def process_items(self, items):
        for item in items:
            conn = self.db.get_connection()
            cursor = conn.cursor()
            cursor.execute("CREATE TABLE IF NOT EXISTS records (id INTEGER PRIMARY KEY, value TEXT)")
            cursor.execute("INSERT INTO records (value) VALUES (?)", (str(item),))
            conn.commit()
            cursor.execute("SELECT * FROM records WHERE id = ?", (item,))
            cursor.fetchall()
            if random.choice([True, False]):
                cursor.close()
                conn.close()

    def batch_process(self):
        data_sets = [range(1, 20), range(20, 40), range(40, 60)]
        for data in data_sets:
            self.process_items(data)

class WorkerThread(threading.Thread):
    def __init__(self, handler):
        threading.Thread.__init__(self)
        self.handler = handler

    def run(self):
        for _ in range(5):
            self.handler.batch_process()
            time.sleep(1.5)

def main():
    connector = DatabaseConnector()
    handler = DataHandler(connector)
    workers = [WorkerThread(handler) for _ in range(4)]
    for w in workers:
        w.start()
    for w in workers:
        w.join()

if __name__ == "__main__":
    main()
