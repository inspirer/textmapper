#   Makefile
#
#   Lapg (Lexical Analyzer and Parser Generator)
#   Copyright (C) 2002-07  Evgeny Gryaznov (inspirer@inbox.ru)
#
#   This program is free software; you can redistribute it and/or modify
#   it under the terms of the GNU General Public License as published by
#   the Free Software Foundation; either version 2 of the License, or
#   (at your option) any later version.
#
#   This program is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU General Public License for more details.
#
#   You should have received a copy of the GNU General Public License
#   along with this program; if not, write to the Free Software
#   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

VER=100

OF=objs/common.o objs/gbuild.o objs/lalr1.o objs/lapg.o objs/lbuild.o objs/parse.o objs/srcgen.o objs/engine.o objs/templates.o objs/defaults.o
SRC=common.cpp gbuild.cpp lalr1.cpp lapg.cpp lbuild.cpp srcgen.cpp engine.cpp
GEN=parse.cpp templates.cpp defaults.cpp
CF=$(SRC) $(GEN)
HF=common.h gbuild.h lalr1.h lbuild.h srcgen.h
PARAMS=-DGCC_COMPILER -O2

lapg : $(OF)
	g++ -o lapg $(PARAMS) $(OF)

objs :
	mkdir objs

objs/%.o : %.cpp $(HF) objs
	g++ -c -o $@ $(PARAMS) $<

clean :
	rm -rf objs lapg errors states parse1.cpp tags txt2c++ templates.cpp defaults.cpp

unix :
	for A in *.cpp *.h syntax; do dos2unix $$A; done

dos :
	for A in *.cpp *.h syntax; do unix2dos $$A; done

templates.cpp: templ_cs templ_cpp templ_js templ_java templ_c templ_text txt2c++
	./txt2c++ templates.cpp templ_cs templ_cpp templ_js templ_java templ_c templ_text

defaults.cpp: default_cpp default_cs default_js default_java default_c txt2c++
	./txt2c++ defaults.cpp default_cpp default_cs default_js default_java default_c
