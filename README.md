# BA
Quellcode für die Bachelorarbeit von Bastian Korte an der TU Dortmund
How to use:
Files are expected to be in cnf, following the following format:
p cnf $VARIABLES $CLAUSES
(-)$INDEX (-)$INDEX ... 0
(-)$INDEX ... 0
.
.
.

Usage of algorithm:
java [Chaff|DPLL|GRASP] $FILENAME (-info)
