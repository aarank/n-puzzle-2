package com.steveinflow.n_puzzle.Solver;

import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;

import com.steveinflow.n_puzzle.GamePlayActivity;
import com.steveinflow.n_puzzle.GameState.GameState;
import com.steveinflow.n_puzzle.Solver.Heuristics.Heuristic;

import java.util.ArrayList;

/**SolveGameTask is a background thread that wraps a Solver.
 * 
 * Created by stepheno on 7/4/13.
 */
public class SolveGameTask extends AsyncTask<Integer, Long, Node> {
    public final String TAG = "SolveGameTask";

    private long mStartTime;
    private Solver mSolver;
    private GamePlayActivity mContext;

    /**If the thread runs longer than this amount, it stops itself.
     * Solving a goal should not take this long.
     */
    public static final long MAX_TIME_ALLOWED = 30000;

    public SolveGameTask(GamePlayActivity context, Heuristic heuristic,
            ArrayList<Point> frozenTiles, GameState gameState){
        mContext = context;


        //strategy is out of goals
        if(heuristic == null || gameState == null){
            this.cancel(true);
        }
        else{
        	Log.d(TAG, "Solvegametask starting for Gamestate: \n" + gameState.toString());
            mSolver = new Solver(this, heuristic, gameState, frozenTiles);
        }
    }

    @Override
    protected Node doInBackground(Integer... args) {
        Log.d(TAG, "Starting solver");

        if(mSolver == null){
            return null;
        }

        mStartTime = System.currentTimeMillis();
        Node result = mSolver.solveGoal();

        return result;
    }

    public void getUpdate(Long... values){
        long timePassed = System.currentTimeMillis() - mStartTime;

        /*if this task has gone on for more than the allowed time, stop the task
         */
        if(timePassed > MAX_TIME_ALLOWED){
            Log.d(TAG, String.format("Solver stopping itself after %s seconds with no solution" +
                    ". %s nodes were checked.", timePassed/1000, values[0]));
            this.cancel(true);
            mContext.solverFailed();
        }

        publishProgress(values);
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        super.onProgressUpdate(values);
        //Long statesChecked = values[0];
        //Log.d(TAG, String.format("Solving... States Checked: %s", statesChecked));
    }

    @Override
    protected void onPostExecute(Node result){
        long runTime = System.currentTimeMillis() - mStartTime;

        if(this.isCancelled()){
            Log.d(TAG, "SolveGameTask cancelled.");
            return;
        }

        if(result == null){
            Log.d(TAG, "SolveGameTask finished, but the result was null");
            mContext.solverFailed();
            return;
        }

        int numMoves = result.getMoveQueue().size();
        Log.d(TAG, String.format("Solver has solved the current goal. It took %s milliseconds. There are " +
                "%s moves in the solution's MoveQueue.",
                runTime, numMoves));
        mContext.processSolvedGoal(result);
    }

}
