import threading
import time
import random

class FileAggregator:
    _all_files = []

    def __init__(self, filenames):
        self.files = [open(f, 'w+') for f in filenames]
        FileAggregator._all_files.extend(self.files)

    def write_lines(self, lines):
        for idx, f in enumerate(self.files):
            f.write(lines[idx % len(lines)] + '\n')

    def manual_cleanup(self):
        for f in self.files:
            f.close()
        self.files = []

    @classmethod
    def global_cleanup(cls):
        for f in cls._all_files:
            try:
                f.close()
            except Exception:
                pass
        cls._all_files.clear()

class LargeCache:
    _cache_block = {}

    def add(self, key, val):
        self._cache_block[key] = val

    def get(self, key):
        return self._cache_block.get(key)

    def clear(self):
        self._cache_block = {}

def perform_resource_heavy_task(iterations):
    agg = FileAggregator([f"tmp_{i}.txt" for i in range(4)])
    texts = ["alpha", "beta", "gamma", "delta"]

    for _ in range(iterations):
        agg.write_lines(texts)
        cache = LargeCache()
        for i in range(10):
            arr = [random.randint(0, 10000) for _ in range(15000)]
            cache.add(i, arr)
        time.sleep(0.01)

class ResourceThread(threading.Thread):
    def run(self):
        perform_resource_heavy_task(3)

def main():
    threads = []
    for _ in range(8):
        t = ResourceThread()
        threads.append(t)
        t.start()

    for t in threads:
        t.join()


if __name__ == "__main__":
    main()
