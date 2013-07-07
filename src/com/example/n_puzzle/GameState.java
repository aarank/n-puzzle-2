package com.example.n_puzzle;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.Arrays;
import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

/**The GameState captures the placements of the tiles.
 *
 * It keeps a 2d array of integers which represent slices of an image.
 * The ints are the order, starting from the top left and going across/down,
 * of the slices of the image before shuffling.
 * The blank tile is represented as -1.
 *
 * The correct placement for a 4 x 4 image is as follows:
 *
 *         0    1   2   3
 *         4    5   6   7
 *         8    9   10  11
 *         12   13  14  -1
 *
 * Created by stepheno on 6/10/13.
 */
public class GameState {
    public final static String TAG = "GameState";

    protected int numDivisions;
    protected int[][] places;
    protected Point blankTile;

    public static enum Direction {
        UP, DOWN, LEFT, RIGHT;
    }


    public GameState(int numDivisions){
        this.numDivisions = numDivisions;
        this.places = new int[numDivisions][numDivisions];
        this.blankTile = new Point(numDivisions - 1, numDivisions - 1);
    }

    public int getNumDivisions(){
        return numDivisions;
    }

    public int getNumTiles(){
        return numDivisions * numDivisions;
    }

    public GameState(int[][] places){
        this.numDivisions = places.length;
        this.places = places;
        this.blankTile = findBlankTile();
    }

    public GameState(int[][] places, Point blankTile){
        this.numDivisions = places.length;
        this.places = places;
        this.blankTile = blankTile;
    }

    /**Create a new GameState by cloning the other*/
    public GameState(GameState other){
        this.numDivisions = other.places.length;
        this.places = new int[numDivisions][numDivisions];
        this.blankTile = new Point();
        this.blankTile.y = other.blankTile.y; this.blankTile.x = other.blankTile.x;

        for(int row = 0; row < numDivisions; row++){
            for(int col = 0; col < numDivisions; col++){
                this.places[row][col] = other.places[row][col];
            }
        }
    }

    /**The default gamestate places the tiles in reverse order. This ensures solvability.
     * If the number of tiles on the board are odd, the final two tiles must be swapped
     * to maintain solvability.
     *
     * @param numDivisions the number of rows or the number of columns in the grid
     * @return a gamestate with tiles placed in reverse order
     */
    public static GameState buildDefaultShuffle(int numDivisions){
        GameState state = new GameState(numDivisions);

        final int NUMBER_OF_TILES = numDivisions * numDivisions - 1;
        int index = NUMBER_OF_TILES - 1;

        for(int row = 0; row < numDivisions; row++){
            for(int col = 0; col < numDivisions; col++){
                state.places[row][col] = index;
                index--;
            }
        }

        /*per the specification- if the number of tiles on the board is odd,
        *we need to swap the 1 and 2 tiles to ensure solvability.
         */
        if(numDivisions % 2  == 0){
            int temp = state.places[numDivisions - 1][numDivisions - 2];
            state.places[numDivisions - 1][numDivisions - 2] = state.places[numDivisions - 1][numDivisions - 3];
            state.places[numDivisions - 1][numDivisions - 3] = temp;
        }

        return state;
    }

    public int getByLocation(int row, int col){
        return places[row][col];
    }

    public Point findActualLocation(int index){
        Point result = new Point();

        for(int row = 0; row < numDivisions; row++){
            for(int col = 0; col < numDivisions; col++){
                if(places[row][col] == index){
                    result = new Point();
                    result.y = row;
                    result.x = col;
                }
            }
        }

        return result;
    }


    @Override
    public boolean equals(Object otherObject){
        GameState other;
        if(otherObject instanceof GameState){
            other = (GameState)otherObject;
        }
        else {
            return false;
        }

        //TODO use deephashcode

        //Log.d(TAG, String.format("Comparing gamestates. \n1: \n%s" +
        //        "\n2: \n%s", this.toString(), other.toString()));

        if(other == null) return false;

        if(places.length != other.places.length) return false;

        for(int row = 0; row < places.length; row++){
            for(int col = 0; col < places[0].length; col++){
                if(places[row][col] != other.places[row][col]){
                    return false;
                }

            }
        }

        return true;

    }


    /**Use deep hash code on the 2d array- this creates a unique
     * hashcode based on the contents of the array*/
    @Override
    public int hashCode() {
        return Arrays.deepHashCode(places);
    }


    /**Return an array list containing legal moves that can be made
     * from this gamestate.
     * Legal moves depend on the location of the blank tile.
     *
     * In addition to the restrictions imposed by the game (the blank tile
     * can't go off the board), the caller can supply a list of tiles that should
     * not be moved. Moves that involve the frozen tiles will not be returned in
     * the list of legal moves.
     *
     * @param frozenTiles a list of tiles that should not be moved.
     * @return a list of moves that can legally be made from this gamestate.
     */
    public ArrayList<Direction> possibleMoves(ArrayList<Point> frozenTiles){
        ArrayList<Direction> moves = new ArrayList<Direction>();
        if(frozenTiles == null) frozenTiles = new ArrayList<Point>();

        findBlankTile();

        Point prospect = null;

        //check first that the blank tile isn't on the edge in each direction.
        //Then check that the tile in the given direction isn't on the frozen list.
        if(blankTile.x > 0){
            prospect = getAdjacent(blankTile, Direction.LEFT);
            if(!frozenTiles.contains(prospect)){
                moves.add(Direction.LEFT);
            }
        }
        if(blankTile.x < places.length - 1){
            prospect = getAdjacent(blankTile, Direction.RIGHT);
            if(!frozenTiles.contains(prospect)){
                moves.add(Direction.RIGHT);
            }
        }
        if(blankTile.y > 0){
            prospect = getAdjacent(blankTile, Direction.UP);
            if(!frozenTiles.contains(prospect)){
                moves.add(Direction.UP);
            }
        }
        if(blankTile.y < places.length - 1){
            prospect = getAdjacent(blankTile, Direction.DOWN);
            if(!frozenTiles.contains(prospect)){
                moves.add(Direction.DOWN);
            }
        }

        return moves;
    }

    /**Get the point adjacent to the given location in the given direction
     *
     * @param targetLocation the Point representing the location of the target
     * @param direction the direction to move one space from the target
     * @return the point adjacent to the given target, in the given direction
     */
    public static Point getAdjacent(Point targetLocation, Direction direction){
        int x = targetLocation.x;
        int y = targetLocation.y;

        switch(direction){
            case UP: y--; break;
            case RIGHT: x++; break;
            case DOWN: y++; break;
            case LEFT: x--; break;
        }
        Point adjacent = new Point();
        adjacent.x = x; adjacent.y = y;
        return adjacent;
    }

    /**Turns a given tile index into a point on the grid
     *
     * @param index the index to convert
     * @param numDivisions the number of rows or columns in the grid
     * @return A point containing the row in y, column in x
     */
    public static Point getCorrectLocationForIndex(int index, int numDivisions) {
        Point destination = new Point();

        //A little math thinking involved
        destination.y = index / numDivisions;
        destination.x = index % numDivisions;
        return destination;
    }

    /**Return the gamestate that would result
     * from moving the tile from the given direction into the blank space.
     *
     * @param move the direction of the tile to slide into the blank space
     * @return the gamestate that would result from making the specified move
     */
    public GameState makeMove(Direction move){
        GameState newState = new GameState(this);

        //start with the blank tile
        int xCoord = blankTile.x;
        int yCoord = blankTile.y;

        //choose the tile 1 space from the blank tile in the given direction
        switch(move){
            case UP: yCoord--; break;
            case DOWN: yCoord++; break;
            case LEFT:xCoord--; break;
            case RIGHT:xCoord++; break;
        }

        if(xCoord < 0 || xCoord >= numDivisions ||
                yCoord < 0 || yCoord >=numDivisions){
            Log.d(TAG, "Error: gameState trying " +
                    "to make a move off the board: " + xCoord + " " + yCoord);
            Log.d(TAG, "State is \n" + this.toString());
            Log.d(TAG, "Move is " + move);
            return null;
        }

        //swap the blank tile with the selected tile
        int temp = newState.places[yCoord][xCoord];
        newState.places[yCoord][xCoord] = -1;
        newState.places[blankTile.y][blankTile.x] = temp;
        newState.blankTile.x =   xCoord;
        newState.blankTile.y = yCoord;

        return newState;

    }

    /**Relocate the blank tile*/
    public Point findBlankTile(){
        for(int row = 0; row < places.length; row++){
            for(int col = 0; col < places[0].length; col++){
                if(places[row][col] == -1){
                    Point p = new Point();
                    p.y = row;
                    p.x = col;
                    blankTile = p;
                    return p;
                }
            }
        }
        return null;
    }

    public Point getBlankTile(){
        return blankTile;
    }


    /**Return a point containing the row in y, col in x of
     * the given tile index.
     */
    public Point getLocation(int index){
        //the blank tile's location is already stored
        if(index == -1){
            return blankTile;
        }

        if(index < -1 || index > (numDivisions * numDivisions - 1)){
            throw new IllegalArgumentException(
                    String.format("getLocation called with bad index %s, numDivisions = %s",
                            + index, numDivisions));
        }

        Point location = new Point();

        //find the target
        for(int row = 0; row < numDivisions; row++){
            for(int col = 0; col < numDivisions; col++){
                if(places[row][col] == index){
                    location.x = col;
                    location.y = row;
                }
            }
        }

        return location;
    }

    @Override
    public String toString(){
        String s = " ";
        for(int row = 0; row < places.length; row++){
            for(int col = 0; col < places.length; col++){
                s = s + " " + places[row][col];
            }
            s+= '\n';
        }

        return s;
    }

}
