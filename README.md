# diagnostic

This program is a diagnostic that evaluates student performance. It is not the standard diagnostic, but is inspired by the Beta model.

The input files are of the following format:
Each line is 3 values. The first is the grade level. So, 3.0 would be the start of 3rd grade, and 4.5 would be half way through 4th grade. The second value is r for right answer or w for wrong answer. The last value is how much weight is given to the question and is always between 0 and 1.

The program evaluates all of the data in the data_holder folder. The evaluation is done line-by-line so you can see how fast the score converges.
