package chess;

import java.util.ArrayList;

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

    /**
     * Plays the next move for whichever player has the turn.
     *
     * @param move String for next move, e.g. "a2 a3"
     *
     * @return A ReturnPlay instance that contains the result of the move.
     *         See the section "The Chess class" in the assignment description for details of
     *         the contents of the returned ReturnPlay instance.
     */
    public static ReturnPlay play(String move) {

        /* FILL IN THIS METHOD */

        /* FOLLOWING LINE IS A PLACEHOLDER TO MAKE COMPILER HAPPY */
        /* WHEN YOU FILL IN THIS METHOD, YOU NEED TO RETURN A ReturnPlay OBJECT */
        return null;
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
        /* FILL IN THIS METHOD */

}