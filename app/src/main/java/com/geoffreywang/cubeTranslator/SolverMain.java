package com.geoffreywang.cubeTranslator;

/**
 * Created by geoffrey_wang on 5/16/17.
 */
public class SolverMain {
    public static void main(String[] args) {
        Solver solver = new Solver(Hand.ORANGE, Hand.WHITE, "U F B' L2 U2 L2 F' B U2 L2 U");
        Long temp = System.currentTimeMillis();
        String solution = solver.generateSolution();
        System.out.println(System.currentTimeMillis() - temp);
        System.out.println(solution);

        printReadableSolution(solution);
    }

    public static void printReadableSolution(String solution){
        for (int i = 0; i < solution.length(); i++) {
            String temp = solution.substring(i, i+1);
            if(temp.equals("0")){
                System.out.println("RIGHT OPEN");
            }else if(temp.equals("1")){
                System.out.println("RIGHT CLOSE");
            }else if(temp.equals("2")){
                System.out.println("RIGHT CLOCKWISE");
            }else if(temp.equals("3")){
                System.out.println("RIGHT COUNTER");
            }else if(temp.equals("4")){
                System.out.println("LEFT OPEN");
            }else if(temp.equals("5")){
                System.out.println("LEFT CLOSE");
            }else if(temp.equals("6")){
                System.out.println("LEFT CLOCKWISE");
            }else if(temp.equals("7")){
                System.out.println("LEFT COUNTER");
            }else if(temp.equals("|")){
                System.out.println();
            }
        }
    }
}
