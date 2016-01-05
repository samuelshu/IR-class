import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;



public class Connect_DB {
	
	static Utilities u = new Utilities();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Connection connection;
		String movie_id = "";
		
	
		try {
			// connect to database
			String url = "jdbc:mysql://140.112.107.207/misdb";
			Class.forName ("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection (url,"user","0000");
			
			// create sql query and get result set
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * From movie");
			
			// show movie info one by one
			while(rs.next()) {
				//movie_score = new TreeMap<String, Integer>();
				movie_id = rs.getString("id");
				String name = rs.getString("name");
				String ser_date = rs.getString("ser_date");
				int total_review = Integer.parseInt(rs.getString("total_review"));
				int total_comment = Integer.parseInt(rs.getString("total_comment"));
				int push = Integer.parseInt(rs.getString("push"));
				int boo = Integer.parseInt(rs.getString("boo"));
				int arrow = Integer.parseInt(rs.getString("arrow"));
				
				if(total_review!=0) {
					// count review score
					GetMovieReviews(movie_id, name, connection);
					//u.log(name+" "+average_score+" "+movie_score.size()); // show "movie_name / movie_score / review_count"
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/* get movie review by movie_id */
	public static void GetMovieReviews(String movie_id, String movie_name, Connection connection) {
		
		int total_score = 0;
		int average_score = 0;
		
		try {
			String sql = "SELECT * FROM review WHERE movie_id = '" + movie_id + "'";
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			Map<String, Integer> movie_score = new TreeMap<String, Integer>(); // record review score
			
			while(rs.next()) {
				String review_id = rs.getString("id");
				String review_title = rs.getString("title");
				int total_comment = Integer.parseInt(rs.getString("total_comment"));
				int push = Integer.parseInt(rs.getString("push"));
				int boo = Integer.parseInt(rs.getString("boo"));
				int arrow = Integer.parseInt(rs.getString("arrow"));
				
				// get “好雷/普雷/負雷” from review title
				String[] review_type = review_title.split("\\[|\\]");
				
				if(review_type.length >= 3) { // split with "[" & "]" will return 3 parts of string 
					if(!review_type[0].contains("Re:") && review_type[1].contains("雷")) {
						int score = ReviewScore(review_type[1], review_title, movie_score, review_id);
						total_score = total_score + score;
					}
				}
			}
			
			// count average movie score
			int review_count = movie_score.size();
			if(review_count > 0) {
				average_score = (total_score/review_count);
				u.writeMapToFile("movie_review/"+movie_name+".txt", movie_score); // save review score as txt
				InsertScoreToDB(movie_id, average_score, movie_score.size(), connection);
				//u.log(movie_name+"\t total_score:"+total_score+"\t average_score:"+average_score+"\t review_count:"+review_count);
			}
			
			rs.close();		
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/* count review score by it's subtitle(review_type) */
	public static int ReviewScore(String review_type, String review_title, Map<String, Integer> map, String index) {
		int score = 0;
		
		if(review_type.contains("好")) {
			if(review_type.contains("大")||review_type.contains("極")||review_type.contains("超")
					|| review_type.contains("爆")||review_type.contains("神")) {
				map.put(index+review_title, 100);
				score = 100;
			} else if(review_type.contains("普")||review_type.contains("還")||review_type.contains("微")
					||review_type.contains("偏")) {
				map.put(index+review_title, 66);
				score = 66;
			} else { // 一般好雷
				map.put(index+review_title, 82);
				score = 82;
			}
		} else if(review_type.contains("普") && !review_type.contains("好")
				&& !review_type.contains("負")) {
			map.put(index+review_title, 50);
			score = 50;
		} else if(review_type.contains("負")||review_type.contains("爛")||review_type.contains("劣")) {
			if(review_type.contains("普")||review_type.contains("微")||review_type.contains("偏")) {
				map.put(index+review_title, 34);
				score = 34;
			} else if(review_type.contains("大")||review_type.contains("極")||review_type.contains("超")
					|| review_type.contains("爆")) {
				map.put(index+review_title, 0);
				score = 0;
			} else { // 一般負雷
				map.put(index+review_title, 18);
				score = 18;
			}
		}
		return score;
	}
	
	
	/* update movie score */
	public static void InsertScoreToDB(String movie_id, int movie_score, int counted_review, Connection connection) throws SQLException
	{
		boolean update;
		String sql = "UPDATE `movie` SET `score` = '"+movie_score+"', `counted_review` = '"+counted_review+"' WHERE `id` = '"+movie_id+"'";
		Statement stmt = connection.createStatement();
		update = stmt.execute(sql);
	}
}
