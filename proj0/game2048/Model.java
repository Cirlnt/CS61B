package game2048;

import java.util.Formatter;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author Cirlnt
 */
public class Model extends Observable {
    /** Current contents of the board. */
    private Board board;
    /** Current score. */
    private int score;
    /** Maximum score so far.  Updated when game ends. */
    private int maxScore;
    /** True iff game is ended. */
    private boolean gameOver;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. Should be deprecated and removed.
     *  */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /** Return the number of squares on one side of the board.
     *  Used for testing. Should be deprecated and removed. */
    public int size() {
        return board.size();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /** Return the current score. */
    public int score() {
        return score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /** Tilt the board toward SIDE. Return true iff this changes the board.
     *
     * 1. If two Tile objects are adjacent in the direction of motion and have
     *    the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     * */
    public boolean tilt(Side side) {
        boolean changed;
        changed = false;

        // TODO: Modify this.board (and perhaps this.score) to account
        // for the tilt to the Side SIDE. If the board changed, set the
        // changed local variable to true.
        // 1. 视角转换：把四个方向的问题简化为“向上移动”
        board.setViewingPerspective(side);

        int len = board.size();

        // 2. 遍历每一列
        for (int c = 0; c < len; c++) {
            // 从倒数第二行开始，自下而上遍历
            // (因为最顶行的格子没法往上走了)
            for (int r = len - 2; r >= 0; r--) {

                Tile current = board.tile(c, r);
                if (current == null) {
                    continue; // 空格子跳过
                }

                // 3. 寻找“碰撞点”
                // 我们想知道这个格子往上走，最终会撞到什么
                int targetR = r + 1;

                // 只要上面是空的，就一直往上找
                while (targetR < len && board.tile(c, targetR) == null) {
                    targetR++;
                }

                // 循环结束后，targetR 指向的是：
                // 情况A: 一个非空格子 (障碍物)
                // 情况B: 越界了 (targetR == len，说明上面全是空的)

                // 4. 判断逻辑

                // 【情况A】：撞到了非空格子，检查是否可以合并
                if (targetR < len && board.tile(c, targetR).value() == current.value()) {
                    // 尝试合并
                    // board.move 返回 true 表示发生了合并
                    if (board.move(c, targetR, current)) {
                        score += current.value() * 2;
                        changed = true;

                        // 【核心修复点】
                        // 合并后，必须立刻结束当前格子的处理！
                        // 否则代码会继续向下执行，导致逻辑错误（如二次合并）
                        continue;
                    }
                }

                // 【情况B 或 无法合并】：普通移动
                // 如果不能合并（撞到了不同数字，或者撞墙了），
                // 我们应该停在“障碍物”的下面一格，也就是 targetR - 1
                targetR--;

                // 只有当目标位置不等于当前位置时，才执行移动
                if (targetR != r) {
                    board.move(c, targetR, current);
                    changed = true;
                }
            }
        }

        // 5. 恢复视角
        board.setViewingPerspective(Side.NORTH);

        checkGameOver();
        if (changed) {
            setChanged();
        }
        return changed;
    }
    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     * */
    public static boolean emptySpaceExists(Board b) {
        int size = b.size();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                    if (b.tile(i, j)== null) {
                        return true;
                    };
                }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        int size = b.size();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (b.tile(i, j)!= null) {
                    if (b.tile(i, j).value()==MAX_PIECE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        if (emptySpaceExists(b)) {
            return true;
        }
        else {
            int size = b.size();
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    Tile current = b.tile(i, j);

                    for (Side s : Side.values()) {
                        int ni = i;
                        int nj = j;

                        if (s == Side.NORTH) {
                            ni = i + 1;
                        } else if (s == Side.SOUTH) {
                            ni = i - 1;
                        } else if (s == Side.EAST) {
                            nj = j + 1;
                        } else if (s == Side.WEST) {
                            nj = j - 1;
                        }
                        if (ni >=0 && nj >= 0 && nj < size && ni < size) {
                            Tile next = b.tile(ni, nj);
                            if (next != null &&next.value() == current.value()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }


    @Override
     /** Returns the model as a string, used for debugging. */
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    /** Returns whether two models are equal. */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    /** Returns hash code of Model’s string. */
    public int hashCode() {
        return toString().hashCode();
    }
}
