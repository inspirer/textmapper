@echo off
if not exist txt2c++.exe cl txt2c++.cpp
txt2c++.exe templates.cpp templ_cs templ_cpp 
txt2c++.exe defaults.cpp default_cpp default_cs