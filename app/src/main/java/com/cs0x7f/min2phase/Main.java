package com.cs0x7f.min2phase;

//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//A simple GUI example to demonstrate how to use the package org.kociemba.twophase

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation, company or business for any purpose whatever) then
 * you should purchase a license for each developer using Jigloo. Please visit www.cloudgarden.com for details. Use of
 * Jigloo implies acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR THIS MACHINE, SO
 * JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class Main {

    // +++++++++++++These variables used only in the GUI-interface+++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private static final long serialVersionUID = 1L;
    static private int maxDepth = 21, maxTime = 5;
    static boolean useSeparator = false;
    static boolean inverse = false;
    static boolean showLength = false;
    static Search search = new Search();

    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public static void main(String[] args) {
        // W, O, B, Y, R, G
        System.out.println(solveCube("YYYYWYYWWGRGWOGWBWGOORBRYBGBORGYWRGBRBRYRBBOOOOBRGGOWW"));
    }

    // +++++++++++++++++++++++++++++++ Generate cube from GUI-Input and solve it ++++++++++++++++++++++++++++++++++++++++
    public static String solveCube(String cube) {
//        for (int i = 0; i < 6; i++)
//            // read the 54 facelets
//            for (int j = 0; j < 9; j++) {
//                if (facelet[i][j].getBackground() == facelet[0][4].getBackground())
//                    s.setCharAt(9 * i + j, 'U');
//                if (facelet[i][j].getBackground() == facelet[1][4].getBackground())
//                    s.setCharAt(9 * i + j, 'R');
//                if (facelet[i][j].getBackground() == facelet[2][4].getBackground())
//                    s.setCharAt(9 * i + j, 'F');
//                if (facelet[i][j].getBackground() == facelet[3][4].getBackground())
//                    s.setCharAt(9 * i + j, 'D');
//                if (facelet[i][j].getBackground() == facelet[4][4].getBackground())
//                    s.setCharAt(9 * i + j, 'L');
//                if (facelet[i][j].getBackground() == facelet[5][4].getBackground())
//                    s.setCharAt(9 * i + j, 'B');
//            }

        String cubeString = cube;
        int mask = 0;
        mask |= useSeparator ? Search.USE_SEPARATOR : 0;
        mask |= inverse ? Search.INVERSE_SOLUTION : 0;
        mask |= showLength ? Search.APPEND_LENGTH : 0;
        long t = System.nanoTime();
        String result = search.solution(cubeString, maxDepth, 100, 0, mask);
        // ++++++++++++++++++++++++ Call Search.solution method from package org.kociemba.twophase ++++++++++++++++++++++++
        while (result.startsWith("Error 8") && ((System.nanoTime() - t) < maxTime * 1.0e9)) {
            result = search.next(100, 0, mask);
        }
        t = System.nanoTime() - t;

        // +++++++++++++++++++ Replace the error messages with more meaningful ones in your language ++++++++++++++++++++++
        if (result.contains("Error")) {
            switch (result.charAt(result.length() - 1)) {
                case '1':
                    result += " : There are not exactly nine facelets of each color!";
                    break;
                case '2':
                    result = " : Not all 12 edges exist exactly once!";
                    break;
                case '3':
                    result = " : Flip error: One edge has to be flipped!";
                    break;
                case '4':
                    result = " : Not all 8 corners exist exactly once!";
                    break;
                case '5':
                    result = " : Twist error: One corner has to be twisted!";
                    break;
                case '6':
                    result = " : Parity error: Two corners or two edges have to be exchanged!";
                    break;
                case '7':
                    result = " : No solution exists for the given maximum move number!";
                    break;
                case '8':
                    result = " : Timeout, no solution found within given maximum time!";
                    break;
            }
        }
        return result;
    }
}
