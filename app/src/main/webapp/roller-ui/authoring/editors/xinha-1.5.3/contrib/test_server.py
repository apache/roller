#!/usr/bin/python

"""Runs a very basic file server so that we can test Xinha.  By default, the
server runs on port 8080, but you can pass the -p or --port option to change
the port used."""

import os
import SimpleHTTPServer
import SocketServer

# File server for testing Xinha

def __main():
    """Use the embed_url.py program from the command-line

    The embed_url.py program downloads files and processes links in the case of
    HTML files.  See embed_url.py -h for more info.  This procedure has the
    sole purpose of reading in and verifying the command-line arguments before
    passing them to the embed_url funtion."""

    from getopt import getopt, GetoptError
    from sys import argv, exit, stderr

    try:
        options, arguments = getopt(argv[1:], "p:", ["port="])
    except GetoptError:
        print "Invalid option"
        __usage()
        exit(2)

    PORT = 8080
    for option, value in options:
        if option in ("-p", "--port"):
            try:
                PORT = int(value)
            except ValueError:
                print "'%s' is not a valid port number" % value
                __usage()
                exit(2)

    # SimpleHTTPRequestHandler serves data from the current directory, so if we
    # are running from inside contrib, we have to change our current working
    # directory
    if os.path.split(os.getcwd())[1] == 'contrib':
        os.chdir('..')

    Handler = SimpleHTTPServer.SimpleHTTPRequestHandler

    httpd = SocketServer.TCPServer(("", PORT), Handler)

    print "Serving at port %s" % PORT
    print "Try viewing the example at http://localhost:%s/examples/Newbie.html" % PORT
    httpd.serve_forever()

def __usage():
    """
    Print the usage information contained in the module docstring
    """
    print __doc__

if __name__ == '__main__':
    __main()
