import java.util.ArrayList;
import java.util.Scanner;

public class Main {
  private static final String PLACE = "x1y1a1 x2y1b1 x3y1c1 x4y1d1 x5y1e1 x6y1f1 x7y1g1 x8y1h1 x9y1i1 x4y2j1 x6y2k1 " +
          "x1y9A1 x2y9B1 x3y9C1 x4y9D1 x5y9E1 x6y9F1 x7y9G1 x8y9H1 x9y9I1 x4y8J1 x6y8K1";
  private static Chessboard Chessboard = new Chessboard();
  private static ArrayList<Piece> Pieces;
  private static char Loser;
  private static boolean NoChance = false;

  public static void main(String[] args) {
    Scanner in = new Scanner(System.in);

    try {
      System.out.println("自动填入棋子:");
      Pieces = Chessboard.placePieces(PLACE);
      Chessboard.show();
    } catch (CanNotPlaceException e) {
      System.out.println("无法放到该格");
      System.exit(1);
    }

    String camp = "red";
    int round = 0;
    int count = 0;

    while (true) {
      if (count % 2 != 1) {
        round++;
      }
      count++;
      System.out.println("第 " + round + " 回合");

      boolean inputError = false;
      boolean haveBattle = false;

      for (int i = 1; i <= 2; i++) {
        do {
          if (camp.equals("red")) {
            System.out.print("红方");
          } else {
            System.out.print("黑方");
          }
          System.out.print("第 " + i + " 次行动:");

          ArrayList action = handleInput(in.nextLine());
          if (action == null) {
            System.out.println("输入有误");
            inputError = true;
            continue;
          }

          int id = (int) action.get(0);
          if (id == 3) {
            int x = (int) action.get(1);
            int y = (int) action.get(2);
            char code = (char) action.get(3);
            inputError = !moveAction(x, y, code, camp, i);
            if (!inputError) {
              Chessboard.show();
            }
          } else if (id == 2) {
            haveBattle = true;
            inputError = !battleAction(action, camp);
            if (!inputError) {
              Chessboard.show();
              if (isGameOver()) {
                System.exit(0);
              }
            }
          } else if (id == 1) {
            Piece piece = (Piece) action.get(1);
            Chessboard.show();
            System.out.println(piece);
            inputError = true;
          }
        } while (inputError);
        NoChance = i == 1 && haveBattle;
      }

      if (camp.equals("red")) {
        camp = "black";
      } else if (camp.equals("black")) {
        camp = "red";
      }
    }
  }

  public static boolean battleAction(ArrayList action, String camp) {
    Piece piece1 = (Piece) action.get(1);
		if (!piece1.getCamp().equals(camp)) {
			System.out.println("你没有这个棋子");
			return false;
		}
    Piece piece2 = (Piece) action.get(2);

    if (piece1.getAttackType() == 0) {
      return frontalBattleAction(piece1, piece2);
    } else {
      return remoteBattleAction(piece1, piece2);
    }
  }

  public static boolean isGameOver() {
    Piece loser = findPiece(Loser);

    if (loser != null) {
      if (loser.getCamp().equals("red") && loser.isKing()) {
        System.out.println("黑方胜利");
        return true;
      } else if (loser.getCamp().equals("black") && loser.isKing()) {
        System.out.println("红方胜利");
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public static ArrayList handleInput(String in) {
    ArrayList action = new ArrayList();
    char tmp[] = in.toCharArray();

    if (tmp.length == 1) {
      action.add(1);

      Piece piece = findPiece(tmp[0]);
      if (piece == null) {
        return null;
      } else {
        action.add(piece);
      }
    } else if (tmp.length == 3) {
      action.add(2);

      Piece piece1 = findPiece(tmp[0]);
      Piece piece2 = findPiece(tmp[2]);

      if (piece1 == null || piece2 == null) {
        return null;
      } else {
        action.add(piece1);
        action.add(piece2);
      }
    } else if (tmp.length == 5) {
      action.add(3);

      try {
        int x = Character.getNumericValue(tmp[1]);
        int y = Character.getNumericValue(tmp[3]);
        char p = tmp[4];

        action.add(x);
        action.add(y);
        action.add(p);
      } catch (NumberFormatException e) {
        return null;
      }
    } else {
      return null;
    }
    return action;
  }

  public static Piece findPiece(char code) {
    for (Piece piece : Pieces) {
      if (piece.getCode() == code) {
        return piece;
      }
    }
    return null;
  }

  public static Piece findPiece(String camp, char code) {
    for (Piece piece : Pieces) {
      if (piece.getCamp().equals(camp)) {
        if (piece.getCode() == code) {
          return piece;
        }
      }
    }
    return null;
  }

  public static boolean remoteBattleAction(Piece piece1, Piece piece2) {
    try {
      Loser = piece1.remoteBattleWith(piece2, Chessboard.getChessboard());
      return true;
    } catch (ExceedAttackRangeException e) {
      System.out.println("超出攻击范围");
      return false;
    } catch (HaveObstacleException e) {
      System.out.println("中间有障碍");
      return false;
    } catch (SameCampException e) {
      System.out.println("这是己方棋子");
      return false;
    } catch (InRiverException e) {
      System.out.println("在河流中无法进行攻击");
      return false;
    } catch (KingSpellException e) {
      System.out.println("国王不受来自对面区域的攻击");
      return false;
    }
  }

  public static boolean frontalBattleAction(Piece piece1, Piece piece2) {
    try {
      Loser = piece1.frontalBattleWith(piece2, Chessboard.getChessboard());
      return true;
    } catch (SameCampException e) {
      System.out.println("这是己方棋子");
      return false;
    } catch (ExceedAttackRangeException e) {
      System.out.println("超出攻击范围");
      return false;
    } catch (InRiverException e) {
      System.out.println("在河流中无法进行攻击");
      return false;
    }
  }

  public static boolean moveAction(int x, int y, char code, String camp, int count) {
    Piece piece = findPiece(camp, code);

    if (piece != null) {
      try {
        char haveChanceChars[] = piece.moveTo(x, y, Chessboard.getChessboard(), count, NoChance);
        if (haveChanceChars != null) {
          for (char haveChanceChar : haveChanceChars) {
            if (haveChanceChar != ' ') {
              Piece haveChancePiece = findPiece(haveChanceChar);
              if (haveChancePiece != null) {
                if (!haveChancePiece.getCamp().equals(piece.getCamp())) {
                  haveChancePiece.opportunityBattleWith(piece, Chessboard.getChessboard());
                }
              }
            }
          }
        }
        return true;
      } catch (CanNotPlaceException e) {
        System.out.println("无法放到该格");
        return false;
      } catch (CanNotMoveException | KingMoveException e) {
        System.out.println("超出移动范围");
        return false;
      }
    } else {
      System.out.println("你没有这个棋子");
      return false;
    }
  }
}
