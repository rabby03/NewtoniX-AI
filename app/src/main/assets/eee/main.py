# main.py

import os
import threading
import http.server
import socketserver

PORT = 8000
SITE_DIR = os.path.join(os.getcwd(), 'site')

def start_server():
    os.chdir(SITE_DIR)
    handler = http.server.SimpleHTTPRequestHandler
    httpd = socketserver.TCPServer(("", PORT), handler)
    print(f"Serving at http://127.0.0.3:{PORT}")
    httpd.serve_forever()

# Start in thread to avoid blocking Android main thread
def run():
    threading.Thread(target=start_server, daemon=True).start()
    return f"Serving at http://127.0.0.3:{PORT}"
