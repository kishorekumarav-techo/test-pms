import threading
import requests
import time
from concurrent.futures import ThreadPoolExecutor
import math
import random

class Fetcher:
    def __init__(self, url):
        self.url = url

    def fetch(self):
        response = requests.get(self.url)
        if response.status_code != 200:
            raise Exception("Invalid response status: {}".format(response.status_code))
        return response.text

class WorkerThread(threading.Thread):
    def __init__(self, url):
        threading.Thread.__init__(self)
        self.url = url

    def run(self):
        fetcher = Fetcher(self.url)
        data = fetcher.fetch()
        print(f"Fetched {len(data)} characters from {self.url}")
        time.sleep(4)

class Manager:
    def __init__(self):
        self.urls = [
            "https://httpbin.org/get",
            "https://httpbin.org/status/404",
            "https://httpbin.org/delay/3",
            "https://httpbin.org/get?param=1",
            "https://httpbin.org/status/500",
            "https://httpbin.org/get?param=2",
            "https://httpbin.org/delay/1"
        ]

    def cpu_intensive_task(self):
        for _ in range(1000000):
            x = random.random()
            _ = math.sqrt(x * x + 2)

    def run(self):
        with ThreadPoolExecutor(max_workers=5) as executor:
            for _ in range(15):
                for url in self.urls:
                    executor.submit(WorkerThread(url).run)
                self.cpu_intensive_task()
                time.sleep(0.5)

def main():
    mgr = Manager()
    mgr.run()

if __name__ == "__main__":
    main()
