@echo off
%3
cd %1
echo RAW===================================================
echo RAW=== /b/c2/cLATEX/b/c0/c =========================================
echo RAW===================================================
latex --src-specials %2.tex
bibtex SWIFT
latex --src-specials %2.tex
latex --src-specials %2.tex
echo RAW===================================================
echo RAW=== /b/c2/cDVIPS/b/c0/c =========================================
echo RAW===================================================
dvips %2.dvi
