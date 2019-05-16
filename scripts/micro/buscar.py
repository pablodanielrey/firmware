import sys
usuario = sys.argv[1]

def check_string():
    archivo=open('ditesi.txt', 'r')
    for line in archivo:
        if usuario in line:
            break
        else:
            print("NO existe")

check_string()