@echo off

if exist .\templates.cpp del /f .\templates.cpp 
if exist .\defaults.cpp del /f .\defaults.cpp
if exist .\out rmdir /q/s .\out
if exist .\out goto error
mkdir .\out

cl /nologo /D "_CRT_SECURE_NO_DEPRECATE" /wd4018 txt2c++.cpp /Fe"out/" /Fo"out/"
.\out\txt2c++.exe templates.cpp templ_cs templ_cpp templ_js templ_java templ_c templ_text
.\out\txt2c++.exe defaults.cpp default_cpp default_cs default_js default_java default_c

cl /nologo /O2 /Ob1 /D "WIN32" /D "NDEBUG" /D "_CONSOLE" /D "_CRT_SECURE_NO_DEPRECATE" /D "_VC80_UPGRADE=0x0600" /D "_MBCS" /GF /FD /EHsc /MT /Gy /Fo".\out/" /Fd".\out/" /W3 /TP /wd4018 /c .\templates.cpp .\srcgen.cpp .\parse.cpp .\lbuild.cpp .\lapg.cpp .\lalr1.cpp .\gbuild.cpp .\engine.cpp .\defaults.cpp .\common.cpp
link /nologo /OUT:"out/lapg.exe" /INCREMENTAL:NO /SUBSYSTEM:CONSOLE /MACHINE:X86 out\common.obj out\defaults.obj out\engine.obj out\gbuild.obj out\lalr1.obj out\lapg.obj out\lbuild.obj out\parse.obj out\srcgen.obj out\templates.obj

if not exist .\out\lapg.exe goto error

move /y .\out\lapg.exe .\bin\

goto ok
:error
echo Error...
:ok
