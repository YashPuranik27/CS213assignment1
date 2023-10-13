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


        if(doesMoveCheckPlayer(currentPlayer, findPiece(currentBoardState, "" + move.charAt(0) + move.charAt(1)), move.charAt(3) + "" + move.charAt(4)))
            return illegal;

        ReturnPlay normalRP = new ReturnPlay();

        executeMove(move);

        if(isCheckMate()){
            if(currentPlayer == Player.white)
                normalRP.message = ReturnPlay.Message.CHECKMATE_WHITE_WINS;
            else
                normalRP.message = ReturnPlay.Message.CHECKMATE_BLACK_WINS;
        } else if(isCheck())
            normalRP.message = ReturnPlay.Message.CHECK;



        //normalRP.message = ...;
        normalRP.piecesOnBoard = currentBoardState;

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

    private static Boolean executeMove(String move){
        return executeMove(currentBoardState, move);
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
        return checkLegal(move, currentPlayer, currentBoardState);
    }
    private static boolean checkLegal(String move, Player playerIn, ArrayList<ReturnPiece> boardIn){
        //Let's assume that an unexpected input also counts as an illegal move
        Pattern pattern = Pattern.compile("[a-h][1-8] [a-h][1-8]", Pattern.CASE_INSENSITIVE);
        Matcher match = pattern.matcher(move.trim());

        if(!match.find())
            return false;

        //If the input is in an expected format, we can go on to ensure that the pieces move correctly:
        // - Consistent with that piece's rules
        // - Consistent with the player's color
        // - Not onto another piece of the same color
        ReturnPiece pieceInPlay = findPiece(boardIn, "" + move.charAt(0) + move.charAt(1));
        ReturnPiece pieceDestination = findPiece(boardIn, "" + move.charAt(3) + move.charAt(4));

        if(pieceInPlay == null)
            return false;

        //Check if white is attempting to move black's pieces
        if(playerIn == Player.white && isBlack(pieceInPlay))
            return false;

        //Check if black is attempting to move white's pieces
        if(playerIn == Player.black && isWhite(pieceInPlay))
            return false;

        if(pieceDestination != null) {
            //Check if white is attempting to move onto another white piece
            if (playerIn == Player.white && isWhite(pieceDestination))
                return false;

            //Check if black is attempting to move onto another black piece
            if (playerIn == Player.black && isBlack(pieceDestination))
                return false;
        }

        //Check if the piece is actually being moved in a direction consistent with its rules/behavior
        if(!isMovementValid(pieceInPlay, "" + move.charAt(3) + move.charAt(4)))
            return false;

        return true;
    }

    private static ReturnPiece findPiece(ArrayList<ReturnPiece> boardState, String location){
        for(ReturnPiece piece : boardState){
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
            if(Math.abs(fileToInt(destination.substring(0,1)) - fileToInt(piece.pieceFile.name())) == 1 && !isWhite(findPiece(currentBoardState, destination)))
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
            if(Math.abs(fileToInt(destination.substring(0,1)) - fileToInt(piece.pieceFile.name())) == 1 && !isBlack(findPiece(currentBoardState, destination)))
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
                if(findPiece(currentBoardState, intToFile(filePos) + "" + rankPos) != null)
                    return false;

                distMoved--;
            }
        }

        //Knight
        if (piece.pieceType == ReturnPiece.PieceType.WN || piece.pieceType == ReturnPiece.PieceType.BN) {
            //If rank changes by 2, file changes by 1. If file changes by 2, rank changes by 1.
            if(Math.abs(fileToInt(piece.pieceFile.name()) - fileToInt(destination.substring(0,1))) == 2 &&
                    Math.abs(piece.pieceRank - Character.getNumericValue(destination.charAt(1))) == 1)
                return true;

            if(Math.abs(fileToInt(piece.pieceFile.name()) - fileToInt(destination.substring(0,1))) == 1 &&
                    Math.abs(piece.pieceRank - Character.getNumericValue(destination.charAt(1))) == 2)
                return true;

            return false;
        }

        //Bishop
        if (piece.pieceType == ReturnPiece.PieceType.WB || piece.pieceType == ReturnPiece.PieceType.BB) {
            //If the difference between starting ranks and files and the final ranks and files are not equal, return false
            if(Math.abs(fileToInt(piece.pieceFile.name()) - fileToInt(destination.substring(0,1))) !=
                    Math.abs(piece.pieceRank - Character.getNumericValue(destination.charAt(1)))) {
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
                if(findPiece(currentBoardState, intToFile(filePos) + "" + rankPos) != null) {
                    return false;
                }
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
                if(findPiece(currentBoardState, intToFile(filePos) + "" + rankPos) != null)
                    return false;

                distMoved--;
            }
        }

        return true;
    }

    private static boolean isCheck(){
        return isCheck(currentBoardState, currentPlayer, false);
    }
    private static boolean isCheck(ArrayList<ReturnPiece> boardIn, Player playerChecked, Boolean onlyCheckPlayerChecked){
        Player enemy = (playerChecked == Player.black ? Player.white : Player.black);
        ReturnPiece king = findKing(playerChecked, boardIn);
        ReturnPiece enemyKing = findKing(enemy, boardIn);
        if (king == null || (enemyKing == null && onlyCheckPlayerChecked == false)) return true;

        for (ReturnPiece piece : boardIn) {
            if((isOpponent(piece, playerChecked) && isMovementValid(piece, "" + king.pieceFile + king.pieceRank))){
                return true;
            }
            if(!onlyCheckPlayerChecked) {
                if ((!isOpponent(piece, playerChecked) && isMovementValid(piece, "" + enemyKing.pieceFile + enemyKing.pieceRank))) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean executeMove(ArrayList<ReturnPiece> tempBoard, String move) {
        ReturnPiece start = findPiece(tempBoard, move.substring(0,2));
        ReturnPiece dest = findPiece(tempBoard, move.substring(3));

        if (start == null) {
            return false; // The start piece is not found, so the move cannot be executed.
        }

        // Making a copy of tempBoard to iterate over and modify tempBoard to avoid ConcurrentModificationException
        ArrayList<ReturnPiece> tempBoardCopy = new ArrayList<>(tempBoard);

        for (ReturnPiece piece : tempBoardCopy) {

            if (piece.pieceRank == start.pieceRank && piece.pieceFile == start.pieceFile) {
                tempBoard.remove(piece);
            }

            if (dest != null && piece.pieceRank == dest.pieceRank && piece.pieceFile == dest.pieceFile) {
                tempBoard.remove(piece);
            }
        }

        ReturnPiece newPiece = new ReturnPiece();
        newPiece.pieceRank = Character.getNumericValue(move.charAt(4));
        newPiece.pieceFile = ReturnPiece.PieceFile.valueOf(move.charAt(3) + "");
        newPiece.pieceType = start.pieceType;

        tempBoard.add(newPiece);

        return true; // The move was executed.
    }
    /*
    private static boolean isCheckOnTempBoard(ArrayList<ReturnPiece> tempBoard) {
        Player enemy = (currentPlayer == Player.black ? Player.white : Player.black);
        ReturnPiece king = findKing(enemy);
        if (king == null) return false;

        for (ReturnPiece piece : tempBoard) {
            if(!isOpponent(piece) && isMovementValid(piece, "" + king.pieceFile + king.pieceRank)){
                return true;
            }
        }
        return false;
    }*/

    private static ReturnPiece copyReturnPiece(ReturnPiece original) {
        ReturnPiece copy = new ReturnPiece();
        copy.pieceType = original.pieceType;
        copy.pieceFile = original.pieceFile;
        copy.pieceRank = original.pieceRank;
        return copy;
    }


    private static boolean doesMoveCheckPlayer(Player playerIn, ReturnPiece piece, String dest){
        if(piece == null){
            return false;
        }
        if (isMovementValid(piece, dest)) {
            // Create a deep copy of currentBoardState

            ArrayList<ReturnPiece> backupBoardState = new ArrayList<>(currentBoardState.size());
            for (ReturnPiece p : currentBoardState) {
                // Assuming a hypothetical copy constructor or clone method in your ReturnPiece class
                backupBoardState.add(copyReturnPiece(p));
            }

            if(!checkLegal("" + piece.pieceFile + piece.pieceRank + " " + dest, playerIn, backupBoardState))
                return true;

            // Execute the move on the temporary board
            executeMove(backupBoardState, "" + piece.pieceFile + piece.pieceRank + " " + dest);

            // Check if the move resolves the check on the temporary board
            return isCheck(backupBoardState, playerIn, true);
        }
        return false;
    }

    private static boolean isCheckMate() {
        ReturnPiece king = findKing(currentPlayer);
        if (king == null) return false;

        for(ReturnPiece piece : currentBoardState) {
            if (isOpponent(piece)) {
                for (int file = 0; file < 8; file++) {
                    for (int rank = 1; rank <= 8; rank++) {
                        String dest = "" + ReturnPiece.PieceFile.values()[file] + rank;
                        if (isMovementValid(piece, dest)) {

                            if (!doesMoveCheckPlayer((currentPlayer == Player.black ? Player.white : Player.black), piece, dest)) {
                                //We're gonna have to verify that this possible position is not also in check, because then it is checkmate
                                return false;
                            }
                            /*
                            // Create a deep copy of currentBoardState

                            ArrayList<ReturnPiece> backupBoardState = new ArrayList<>(currentBoardState.size());
                            for (ReturnPiece p : currentBoardState) {
                                // Assuming a hypothetical copy constructor or clone method in your ReturnPiece class
                                backupBoardState.add(copyReturnPiece(p));
                            }

                            if(!checkLegal("" + piece.pieceFile + piece.pieceRank + " " + dest, (currentPlayer == Player.black ? Player.white : Player.black), backupBoardState))
                                continue;

                            // Execute the move on the temporary board
                            executeMove(backupBoardState, "" + piece.pieceFile + piece.pieceRank + " " + dest);

                            // Check if the move resolves the check on the temporary board
                            //If no legal moves are found, this is checkmate
                            boolean stillInCheck = isCheck(backupBoardState, (currentPlayer == Player.black ? Player.white : Player.black), true);
                            // If the move resolves the check, it's not checkmate
                            if (!stillInCheck) {
                                //We're gonna have to verify that this possible position is not also in check, because then it is checkmate
                                return false;
                            }
                        }
                        */
                        }
                    }
                }
            }
        }

        return true;
    }

    private static ReturnPiece findKing(Player player){
        return findKing(player, currentBoardState);
    }
    private static ReturnPiece findKing(Player player, ArrayList<ReturnPiece> boardIn){
        ReturnPiece.PieceType kingType = (player == Player.white) ? ReturnPiece.PieceType.WK : ReturnPiece.PieceType.BK;
        for (ReturnPiece piece : boardIn) {
            if (piece.pieceType == kingType) {
                return piece;
            }
        }
        return null; // This should not happen if the board is valid
    }

    private static boolean isOpponent(ReturnPiece piece){
        return isOpponent(piece, currentPlayer);
    }

    private static boolean isOpponent(ReturnPiece piece, Player playerIn){
        if (piece == null) return false;
        return (playerIn == Player.white && isBlack(piece)) || (playerIn == Player.black && isWhite(piece));
    }

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