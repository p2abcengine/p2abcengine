MINGWPATH=~/Development/MinGW/bin
PREFIX=i686-w64-mingw32-

all:		abcetray.o abcetray.c
		${MINGWPATH}/${PREFIX}gcc -o build/ABCETray.exe abcetray.c build/abcetray.o -Wall -mwindows -lws2_32 -O2 -s
		cp build/ABCETray.exe /home/michael/Downloads

debug:		abcetray.o abcetray.c
		${MINGWPATH}/${PREFIX}gcc -o build/ABCETray.exe abcetray.c build/abcetray.o -Wall -lws2_32 -s
		cp build/ABCETray.exe /home/michael/Downloads

abcetray.o:	include/abcetray.rc
		${MINGWPATH}/${PREFIX}windres include/abcetray.rc build/abcetray.o

clean:
		rm -f build/abcetray.o build/ABCETray.exe
