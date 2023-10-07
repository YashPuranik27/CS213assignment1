package chess;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ReturnPiece {
    static enum PieceType {WP, WR, WN, WB, WQ, WK,
        BP, BR, BN, BB, BK, BQ};
    static enum PieceFile {a, b, c, d, e, f, g, h};

    PieceType pieceType;
    PieceFile pieceFile;
    int pieceRank;  // 1..8
    public String toString() {
        return ""+pieceFile+pieceRank+":"+pieceType;
    }
    public boolean equals(Object other) {
        if (other == null || !(other instanceof ReturnPiece)) {
            return false;
        }
        ReturnPiece otherPiece = (ReturnPiece)other;
        return pieceType == otherPiece.pieceType &&
                pieceFile == otherPiece.pieceFile &&
                pieceRank == otherPiece.pieceRank;
    }
}

class ReturnPlay {
    enum Message {ILLEGAL_MOVE, DRAW,
        RESIGN_BLACK_WINS, RESIGN_WHITE_WINS,
        CHECK, CHECKMATE_BLACK_WINS,	CHECKMATE_WHITE_WINS,
        STALEMATE};

    ArrayList<ReturnPiece> piecesOnBoard;
    Message message;
}

public class Chess {

    enum Player { white, black }
    static Player currentPlayer = Player.white; //White goes first

    /**
     * Plays the next move for whichever player has the turn.
     *
     * @param move String for next move, e.g. "a2 a3"
     *
     * @return A ReturnPlay instance that contains the result of the move.
     *         See the section "The Chess class" in the assignment description for details of
     *         the contents of the returned ReturnPlay instance.
     */

    private static int turnNumber = 0;
    public static ReturnPlay play(String move) {

        ReturnPlay exiting = new ReturnPlay();

        if(move.equalsIgnoreCase("resign")){
            if(currentPlayer == Player.white)
                exiting.message = ReturnPlay.Message.RESIGN_BLACK_WINS;
            else
                exiting.message = ReturnPlay.Message.RESIGN_WHITE_WINS;

            return exiting;
        }

        boolean draw = false;

        if(move.contains("draw?")){
            draw = true;
            exiting.message = ReturnPlay.Message.DRAW;

            move = move.replace(" draw?", "");
        }

        //We're gonna first check to make sure the move is legal, if it isn't there's no point in continuing
        ReturnPlay illegal = new ReturnPlay();
        illegal.message = ReturnPlay.Message.ILLEGAL_MOVE;
        illegal.piecesOnBoard = currentBoardState;

        if(!checkLegal(move))
            return illegal;

        executeMove(move);

        ReturnPlay normalRP = new ReturnPlay();
        //normalRP.message = ...;
        normalRP.piecesOnBoard = currentBoardState;

        if(isCheckMate()){
            if(currentPlayer == Player.white)
                normalRP.message = ReturnPlay.Message.CHECKMATE_WHITE_WINS;
            else
                normalRP.message = ReturnPlay.Message.CHECKMATE_BLACK_WINS;
        } else if(isCheck())
            normalRP.message = ReturnPlay.Message.CHECK;

        if(turnNumber == 1)
            pawnMaxMovement = 1;

        if(currentPlayer == Player.white)
            currentPlayer = Player.black;
        else
            currentPlayer = Player.white;

        if(currentPlayer == Player.black) //At the end of black's move, it's the next turn/round
            turnNumber++;

        if(draw)
            return exiting;

        return normalRP;
    }

    private static void executeMove(String move){
        ReturnPiece start = findPiece(move.substring(0,2)); //Should never be null
        ReturnPiece dest = findPiece(move.substring(3));
        for(int i = 0; i < currentBoardState.size(); i++){
            ReturnPiece piece = currentBoardState.get(i);
            System.out.println("DEBUG " + move.substring(0,2));
            if(piece.pieceRank == start.pieceRank &&
                    piece.pieceFile == start.pieceFile)
                currentBoardState.remove(piece);

            if(dest != null)
                if(piece.pieceRank == dest.pieceRank && piece.pieceFile == dest.pieceFile)
                    currentBoardState.remove(findPiece(move.substring(3)));
        }

        ReturnPiece newPiece = new ReturnPiece();
        newPiece.pieceRank = Character.getNumericValue(move.charAt(4));
        newPiece.pieceFile = ReturnPiece.PieceFile.valueOf(move.charAt(3)+"");
        newPiece.pieceType = start.pieceType;

        currentBoardState.add(newPiece);
    }

    private static int fileToInt(String file){
        file = file.toLowerCase();
        return ((int) file.charAt(0)) - ((int) 'a');
    }

    private static String intToFile(int file){ //0 is A, 1 B, 2 C...
        int starting = (int) 'a';
        return ("" + ((char) (starting+file)));
    }

    private static boolean isBlack(ReturnPiece in){
        if(in == null)
            return false;

        return (in.pieceType == ReturnPiece.PieceType.BR ||
                in.pieceType == ReturnPiece.PieceType.BN ||
                in.pieceType == ReturnPiece.PieceType.BB ||
                in.pieceType == ReturnPiece.PieceType.BQ ||
                in.pieceType == ReturnPiece.PieceType.BK ||
                in.pieceType == ReturnPiece.PieceType.BP);
    }

    private static boolean isWhite(ReturnPiece in){
        if(in == null)
            return false;

        return (in.pieceType == ReturnPiece.PieceType.WR ||
                in.pieceType == ReturnPiece.PieceType.WN ||
                in.pieceType == ReturnPiece.PieceType.WB ||
                in.pieceType == ReturnPiece.PieceType.WQ ||
                in.pieceType == ReturnPiece.PieceType.WK ||
                in.pieceType == ReturnPiece.PieceType.WP);
    }
    private static boolean checkLegal(String move){
        //Let's assume that an unexpected input also counts as an illegal move
        Pattern pattern = Pattern.compile("[a-h][1-8] [a-h][1-8]", Pattern.CASE_INSENSITIVE);
        Matcher match = pattern.matcher(move.trim());

        if(!match.find())
            return false;

        //If the input is in an expected format, we can go on to ensure that the pieces move correctly:
        // - Consistent with that piece's rules
        // - Consistent with the player's color
        // - Not onto another piece of the same color
        ReturnPiece pieceInPlay = findPiece("" + move.charAt(0) + move.charAt(1));
        ReturnPiece pieceDestination = findPiece("" + move.charAt(3) + move.charAt(4));

        if(pieceInPlay == null)
            return false;

        //Check if white is attempting to move black's pieces
        if(currentPlayer == Player.white && isBlack(pieceInPlay))
            return false;

        //Check if black is attempting to move white's pieces
        if(currentPlayer == Player.black && isWhite(pieceInPlay))
            return false;

        if(pieceDestination != null) {
            //Check if white is attempting to move onto another white piece
            if (currentPlayer == Player.white && isWhite(pieceDestination))
                return false;

            //Check if black is attempting to move onto another black piece
            if (currentPlayer == Player.black && isBlack(pieceDestination))
                return false;
        }

        //Check if the piece is actually being moved in a direction consistent with its rules/behavior
        if(!isMovementValid(pieceInPlay, "" + move.charAt(3) + move.charAt(4)))
            return false;

        return true;
    }

    private static ReturnPiece findPiece(String location){
        for(ReturnPiece piece : currentBoardState){
            String mLocation = "" + piece.pieceFile.name() + piece.pieceRank;
            if(mLocation.equals(location)){
                return piece;
            }
        }
        return null;
    }

    private static int pawnMaxMovement = 2;
    private static boolean isMovementValid(ReturnPiece piece, String destination){

        //Black Pawn
        if(piece.pieceType == ReturnPiece.PieceType.BP){ //Black pawn can only move down
            //if rank moves anything greater than (-pawnMaxMovement), return false
            if(Character.getNumericValue(destination.charAt(1)) - piece.pieceRank < -pawnMaxMovement ||
                    Character.getNumericValue(destination.charAt(1)) - piece.pieceRank > 0)
                return false;

            //if file moves over by 1 in either direction, but there is no white piece at destination, return false
            if(Math.abs(fileToInt(destination.substring(0,1)) - fileToInt(piece.pieceFile.name())) == 1 && !isWhite(findPiece(destination)))
                return false;

            //otherwise if file moves, return false
            if(destination.charAt(0) != piece.pieceFile.name().charAt(0))
                return false;
        }

        //White Pawn
        if(piece.pieceType == ReturnPiece.PieceType.WP){ //White pawn can only move up
            //if rank moves anything greater than (-pawnMaxMovement), return false
            if(Character.getNumericValue(destination.charAt(1)) - piece.pieceRank > pawnMaxMovement ||
                    Character.getNumericValue(destination.charAt(1)) - piece.pieceRank < 0)
                return false;

            //if file moves over by 1 in either direction, but there is no black piece at destination, return false
            if(Math.abs(fileToInt(destination.substring(0,1)) - fileToInt(piece.pieceFile.name())) == 1 && !isBlack(findPiece(destination)))
                return false;

            //if file moves more than 1, return false
            if(destination.charAt(0) != piece.pieceFile.name().charAt(0))
                return false;
        }

        //Rook
        if (piece.pieceType == ReturnPiece.PieceType.WR || piece.pieceType == ReturnPiece.PieceType.BR) {
            //If both rank and file change, return false
            if(piece.pieceFile.name().charAt(0) != destination.charAt(0) && piece.pieceRank != Character.getNumericValue(destination.charAt(1)))
                return false;

            //If any piece is in the path from start to finish (excluding final, that's checked for prior), return false
            //if starting file is behind destination, we're going right
            int fileIncrement = (fileToInt(piece.pieceFile.name()) - fileToInt(destination.substring(0,1)) <= 0 ? 1 : -1);
            fileIncrement = fileIncrement * (fileToInt(piece.pieceFile.name()) - fileToInt(destination.substring(0,1)) == 0 ? 0 : 1);
            int filePos = fileToInt(piece.pieceFile.name());

            //if starting rank is less than dest, we're going up
            int rankIncrement = (piece.pieceRank - Character.getNumericValue(destination.charAt(1)) <= 0 ? 1 : -1);
            rankIncrement = rankIncrement * (piece.pieceRank * Character.getNumericValue(destination.charAt(1)) == 0 ? 0 : 1);
            int rankPos = piece.pieceRank;

            //We can add the differences between rank and file together here because one of them will always be 0.
            int distMoved = Math.abs(piece.pieceRank - Character.getNumericValue(destination.charAt(1)) + fileToInt(piece.pieceFile.name()) - fileToInt(destination.substring(0,1)));

            while(distMoved > 1){ //My logic is telling me this should be 1, but it's maybe 0. Try 0 if it bugs.
                rankPos += rankIncrement; //Again, one of these increments will always be 0.
                filePos += fileIncrement;

                //This should be fine because the distMoved >1 excludes the final piece in the path (destination).
                if(findPiece(intToFile(filePos) + "" + rankPos) != null)
                    return false;

                distMoved--;
            }
        }

        //Knight
        if (piece.pieceType == ReturnPiece.PieceType.WN || piece.pieceType == ReturnPiece.PieceType.BN) {
            //If rank changes by 2, file changes by 1. If file changes by 2, rank changes by 1.
            if(Math.abs(fileToInt(piece.pieceFile.name()) - fileToInt(destination.substring(0,1))) == 2 &&
                    Math.abs(piece.pieceRank - Character.getNumericValue(destination.charAt(1))) != 1)
                return false;

            if(Math.abs(fileToInt(piece.pieceFile.name()) - fileToInt(destination.substring(0,1))) == 1 &&
                    Math.abs(piece.pieceRank - Character.getNumericValue(destination.charAt(1))) != 2)
                return false;
        }

        //Bishop
        if (piece.pieceType == ReturnPiece.PieceType.WB || piece.pieceType == ReturnPiece.PieceType.BB) {
            //If the difference between starting ranks and files and the final ranks and files are not equal, return false
            if(Math.abs(fileToInt(piece.pieceFile.name()) - fileToInt(destination.substring(0,1))) !=
                    Math.abs(piece.pieceRank - Character.getNumericValue(destination.charAt(1))))
                return false;

            //If any piece is in the path from start to finish (excluding final, that's checked for prior), return false
            //if starting file is behind destination, we're going right
            int fileIncrement = (fileToInt(piece.pieceFile.name()) - fileToInt(destination.substring(0,1)) < 0 ? 1 : -1);
            int filePos = fileToInt(piece.pieceFile.name());

            //if starting rank is less than dest, we're going up
            int rankIncrement = (piece.pieceRank - Character.getNumericValue(destination.charAt(1)) < 0 ? 1 : -1);
            int rankPos = piece.pieceRank;

            int distMoved = Math.abs(piece.pieceRank - Character.getNumericValue(destination.charAt(1)));

            while(distMoved > 1){ //My logic is telling me this should be 1, but it's maybe 0. Try 0 if it bugs.
                rankPos += rankIncrement;
                filePos += fileIncrement;

                //This should be fine because the distMoved >1 excludes the final piece in the path (destination).
                if(findPiece(intToFile(filePos) + "" + rankPos) != null)
                    return false;

                distMoved--;
            }
        }

        //King
        if (piece.pieceType == ReturnPiece.PieceType.WK || piece.pieceType == ReturnPiece.PieceType.BK) {
            //If rank or file change more than 1 in any direction, return false
            if(Math.abs(fileToInt(piece.pieceFile.name()) - fileToInt(destination.substring(0,1))) > 1 ||
                    Math.abs(piece.pieceRank - Character.getNumericValue(destination.charAt(1))) > 1)
                return false;
        }

        //Queen
        if (piece.pieceType == ReturnPiece.PieceType.WQ || piece.pieceType == ReturnPiece.PieceType.BQ) {

            //Determine if queen will be moving like a rook or like a bishop this move.
            boolean bishopMode = (piece.pieceFile.name().charAt(0) != destination.charAt(0) && piece.pieceRank != Character.getNumericValue(destination.charAt(1)) );
            if(bishopMode){
                if(Math.abs(fileToInt(piece.pieceFile.name()) - fileToInt(destination.substring(0,1))) !=
                        Math.abs(piece.pieceRank - Character.getNumericValue(destination.charAt(1))))
                    return false;
            }

            //If any piece is in the path from start to finish (excluding final, that's checked for prior), return false
            //if starting file is behind destination, we're going right
            int fileIncrement = (fileToInt(piece.pieceFile.name()) - fileToInt(destination.substring(0,1)) < 0 ? 1 : -1);
            int filePos = fileToInt(piece.pieceFile.name());

            //if starting rank is less than dest, we're going up
            int rankIncrement = (piece.pieceRank - Character.getNumericValue(destination.charAt(1)) < 0 ? 1 : -1);
            int rankPos = piece.pieceRank;

            int distMoved = Math.abs(piece.pieceRank - Character.getNumericValue(destination.charAt(1)));

            while(distMoved > 1){ //My logic is telling me this should be 1, but it's maybe 0. Try 0 if it bugs.
                rankPos += rankIncrement;
                filePos += fileIncrement;

                //This should be fine because the distMoved >1 excludes the final piece in the path (destination).
                if(findPiece(intToFile(filePos) + "" + rankPos) != null)
                    return false;

                distMoved--;
            }
        }

        return true;
    }
    private static boolean isCheck(){

        return false;
    }

    private static boolean isCheckMate(){

        return false;
    }

    /**
     * This method should reset the game, and start from scratch.
     */
    static ArrayList<ReturnPiece> currentBoardState;
    public static void start() {
        currentBoardState = new ArrayList<>();

        // White Pieces
        for(ReturnPiece.PieceFile file: ReturnPiece.PieceFile.values()) {
            // White Pawns
            ReturnPiece whitePawn = new ReturnPiece();
            whitePawn.pieceType = ReturnPiece.PieceType.WP;
            whitePawn.pieceFile = file;
            whitePawn.pieceRank = 2;
            currentBoardState.add(whitePawn);
        }

        addPiece(currentBoardState, ReturnPiece.PieceType.WR, ReturnPiece.PieceFile.a, 1);
        addPiece(currentBoardState, ReturnPiece.PieceType.WN, ReturnPiece.PieceFile.b, 1);
        addPiece(currentBoardState, ReturnPiece.PieceType.WB, ReturnPiece.PieceFile.c, 1);
        addPiece(currentBoardState, ReturnPiece.PieceType.WQ, ReturnPiece.PieceFile.d, 1);
        addPiece(currentBoardState, ReturnPiece.PieceType.WK, ReturnPiece.PieceFile.e, 1);
        addPiece(currentBoardState, ReturnPiece.PieceType.WB, ReturnPiece.PieceFile.f, 1);
        addPiece(currentBoardState, ReturnPiece.PieceType.WN, ReturnPiece.PieceFile.g, 1);
        addPiece(currentBoardState, ReturnPiece.PieceType.WR, ReturnPiece.PieceFile.h, 1);

        // Black Pieces
        for(ReturnPiece.PieceFile file: ReturnPiece.PieceFile.values()) {
            // Black Pawns
            ReturnPiece blackPawn = new ReturnPiece();
            blackPawn.pieceType = ReturnPiece.PieceType.BP;
            blackPawn.pieceFile = file;
            blackPawn.pieceRank = 7;
            currentBoardState.add(blackPawn);
        }

        addPiece(currentBoardState, ReturnPiece.PieceType.BR, ReturnPiece.PieceFile.a, 8);
        addPiece(currentBoardState, ReturnPiece.PieceType.BN, ReturnPiece.PieceFile.b, 8);
        addPiece(currentBoardState, ReturnPiece.PieceType.BB, ReturnPiece.PieceFile.c, 8);
        addPiece(currentBoardState, ReturnPiece.PieceType.BQ, ReturnPiece.PieceFile.d, 8);
        addPiece(currentBoardState, ReturnPiece.PieceType.BK, ReturnPiece.PieceFile.e, 8);
        addPiece(currentBoardState, ReturnPiece.PieceType.BB, ReturnPiece.PieceFile.f, 8);
        addPiece(currentBoardState, ReturnPiece.PieceType.BN, ReturnPiece.PieceFile.g, 8);
        addPiece(currentBoardState, ReturnPiece.PieceType.BR, ReturnPiece.PieceFile.h, 8);

        // Print the initialized board.
        PlayChess.printBoard(currentBoardState);
    }

    private static void addPiece(ArrayList<ReturnPiece> board, ReturnPiece.PieceType type, ReturnPiece.PieceFile file, int rank) {
        ReturnPiece piece = new ReturnPiece();
        piece.pieceType = type;
        piece.pieceFile = file;
        piece.pieceRank = rank;
        board.add(piece);
    }

}