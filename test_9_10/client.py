import threading
import requests

class Fetcher:
    def __init__(self, url):
        self.url = url

    def fetch(self):
        response = requests.get(self.url)
        if response.status_code != 200:
            raise Exception("Status " + str(response.status_code))
        return response.json()

class Task(threading.Thread):
    def __init__(self, url):
        threading.Thread.__init__(self)
        self.url = url

    def run(self):
        client = Fetcher(self.url)
        data = client.fetch()
        if data is None:
            print("Empty data")

def main():
    urls = [
        "https://httpbin.org/status/500",
        "https://httpbin.org/status/404",
        "https://httpbin.org/status/200"
    ]
    threads = []
    for url in urls:
        for _ in range(5):
            t = Task(url)
            threads.append(t)
            t.start()
    for t in threads:
        t.join()

if __name__ == "__main__":
    main()
