package com.geoffreywang.cubeTranslator;

import java.util.Arrays;

public class Solver {

    //CONSTANTS
    public static final int CLOCKWISE = 0, COUNTER = 1, DOUBLE = 2;
    private static final int RIGHT_MOVE_CODE_OFFSET = 0, LEFT_MOVE_CODE_OFFSET = 4;

    //Instance Fields
    private Hand left, right;
    private String solution;
    private String moveCode;

    //Constructor
    public Solver(int leftColor, int rightColor, String solution) {
        left = new Hand(leftColor, LEFT_MOVE_CODE_OFFSET);
        right = new Hand(rightColor, RIGHT_MOVE_CODE_OFFSET);
        this.solution = convertSolution(solution);
    }

    /**
     * Turns the cube based on hand and direction given
     * @param direction Direction of the turn (utilize constants)
     * @param isRight Denotes which hand to use
     * @return Move code for turning the cube
     */
    public String turnCube(int direction, boolean isRight){
        //Sets Current Converter.Hand and Alternative Converter.Hand based on selection
        Hand currentHand, altHand;
        if(isRight){
            currentHand = right;
            altHand = left;
        }else{
            currentHand = left;
            altHand = right;
        }

        //Grab color info from hands
        int currentHandColor = currentHand.getColor();
        int altHandColor = altHand.getColor();
        int altColorIndex = Arrays.asList(currentHand.getColorArray()).indexOf(altHandColor);

        //Chooses array offset for "turning" the cube
        int offset;
        if(direction == CLOCKWISE){
            if(currentHandColor == Hand.BLUE || currentHandColor == Hand.RED || currentHandColor == Hand.WHITE){
                offset = 1;
            }else{
                offset = -1;
            }
        }else if(direction == COUNTER){
            if(currentHandColor == Hand.BLUE || currentHandColor == Hand.RED || currentHandColor == Hand.WHITE){
                offset = -1;
            }else{
                offset = 1;
            }
        }else{
            offset = 2;
        }

        //Sets altColorIndex based on offset
        altColorIndex += offset;
        altColorIndex = Solver.mod(altColorIndex,4);
        altHand.setColor(currentHand.getColorArray()[altColorIndex]);
        //Create move code for turning the cube/turn the cube
        String returnString = "";
        //Open hand
        returnString += altHand.open() + "|";
        //Correct Alternative hand if needed
        if(altHand.isInverted()){
            returnString += altHand.turnClockwise();
        }
        //Turn hand
        if(direction == CLOCKWISE){
            returnString += currentHand.turnClockwise();
        }else if(direction == COUNTER){
            returnString += currentHand.turnCounter();
        }else{
            returnString += currentHand.turnDouble();
        }
        //Close hand
        returnString += "|" + altHand.close() + "|";
        return returnString;
    }

    /**
     * Colors in each of the hands
     * @return Converter.Hand colors
     */
    @Override
    public String toString() {
        return "RIGHT: " + right +" LEFT: " + left;
    }

    /**
     * Turn cube to target
     * @param target Target color
     * @return Move code for turning to target color
     */
    public String turnToColor(int target){
        //Check if the cube is already on target
        if(left.getColor() == target || right.getColor() == target){
            return "";
        }else {
            //Focus on inverted hand if one exists, else default to the left hand
            boolean isFocusRight;
            if (left.isInverted()) {
                isFocusRight = false;
            } else if (right.isInverted()) {
                isFocusRight = true;
            } else {
                isFocusRight = false;
            }

            //Populate focusedHand base on boolean
            Hand focusedHand;
            if(isFocusRight){
                focusedHand = right;
            }else{
                focusedHand = left;
            }

            //Check if color exist on the focusedHand colorArray, else switch hand
            if (Arrays.asList(focusedHand.getColorArray()).indexOf(target) == -1) {
                isFocusRight = !isFocusRight;
            }

            //Populate focusedHand and otherhand base on updated boolean
            Hand otherHand;
            if(isFocusRight){
                focusedHand = right;
                otherHand = left;
            }else{
                focusedHand = left;
                otherHand = right;
            }

            //Grab color info from hands
            int otherHandColorIndex = Arrays.asList(focusedHand.getColorArray()).indexOf(otherHand.getColor());
            int targetColorIndex = Arrays.asList(focusedHand.getColorArray()).indexOf(target);

            //Calculate rotation offset
            int turnNumber = Solver.mod(targetColorIndex - otherHandColorIndex, 4);

            //Categorize correct turn value and return corrisponding move code
            if (turnNumber == 2) { //Turn the focused hand twice
                return turnCube(DOUBLE,isFocusRight);
            }else{ //Determine direction of turn
                if (focusedHand.getColor() == Hand.BLUE || focusedHand.getColor() == Hand.RED || focusedHand.getColor() == Hand.WHITE) {
                    if (turnNumber == 1){
                        return turnCube(CLOCKWISE,isFocusRight);
                    }else{
                        return turnCube(COUNTER,isFocusRight);
                    }
                } else {
                    if (turnNumber == 3){
                        return turnCube(CLOCKWISE,isFocusRight);
                    }else{
                        return turnCube(COUNTER,isFocusRight);
                    }
                }
            }
        }
    }

    /**
     * Modulus method for indexOutOfBounds correction
     * @param num Number to be modded
     * @param mod Modulus
     * @return corrected index
     */
    public static int mod(int num, int mod){
        return ((num % mod) + mod) % mod;
    }

    /**
     * Generates move code based on algorithm
     * @return moveCode
     */
    public String generateSolution(){
        //Checks if moveCode isn't already generated
        if(moveCode == null){
            moveCode = "";

            //Splits the algorithm to moves
            String[] moves = solution.split("\\s+");

            //Runs through algorithm and generates moveCode
            for (int i = 0; i < moves.length; i++) {
                //Extracts targetColor from the move
                int targetColor = Integer.valueOf(moves[i].substring(0,1));
                //Adds moveCode based on required movement
                //   Order of operation:
                //       - Turn to face
                //       - Correct inversion in needed
                //       - Turn face

                //Turn to face
                moveCode += turnToColor(targetColor);

                //Choose correct hand
                Hand colorHand;
                boolean checkLeft;
                if(left.getColor() == targetColor){
                    colorHand = left;
                    checkLeft = false;
                }else{
                    colorHand = right;
                    checkLeft = true;
                }

                //Correct otherHand if needed
                moveCode += fixInverted(checkLeft);

                //Turn face
                if(moves[i].length() > 1){
                    String direction = moves[i].substring(1);
                    if(direction.equals("'")){
                        moveCode += colorHand.turnCounter();
                    }else{
                        moveCode += colorHand.turnDouble();
                    }
                }else{
                    moveCode += colorHand.turnClockwise();
                }
                moveCode += "|";
            }
        }
        return moveCode;
    }

    /**
     * Check if hand is inverted, if so return moveCode that corrects for it
     * @param fixLeftHand boolean regarding which hand to check
     * @return Correction moveCode
     */
    public String fixInverted(boolean fixLeftHand){
        //Extracts correct hand to check
        Hand handToCheck;
        if(fixLeftHand){
            handToCheck = left;
        }else{
            handToCheck = right;
        }

        //Check if hand is normal
        if(!handToCheck.isInverted()){
            return "";
        }

        //Correct hand (default to clockwise motion)
        String returnString = "";
        returnString += handToCheck.open() + "|";
        returnString += handToCheck.turnClockwise() + "|";
        returnString += handToCheck.close() + "|";
        return returnString;
    }

    /**
     * Changes the letters used in the solution algorithm for numbers regarding faces
     * @param solution
     * @return solution in number form
     */
    public static String convertSolution(String solution){
        //Split the solution into individual moves
        String[] moves = solution.split("\\s+");

        //Creates a new string that uses numbers instead
        String returnString = "";
        for (int i = 0; i < moves.length; i++) {
            //Replaces the face with numbers
            if(moves[i].substring(0,1).equals("F")){
                returnString += Hand.BLUE;
            }else if(moves[i].substring(0,1).equals("R")){
                returnString += Hand.ORANGE;
            }else if(moves[i].substring(0,1).equals("L")){
                returnString += Hand.RED;
            }else if(moves[i].substring(0,1).equals("U")){
                returnString += Hand.WHITE;
            }else if(moves[i].substring(0,1).equals("D")){
                returnString += Hand.YELLOW;
            }else{
                returnString += Hand.GREEN;
            }

            //Appends the ' and 2 data
            if(moves[i].length() > 1){
                returnString += moves[i].substring(1);
            }

            //Separates the string with spaces
            returnString += " ";
        }
        return returnString;
    }
}
