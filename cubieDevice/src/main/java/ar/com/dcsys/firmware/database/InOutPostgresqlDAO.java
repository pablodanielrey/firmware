package ar.com.dcsys.firmware.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import ar.com.dcsys.firmware.database.InOut.Status;
import ar.com.dcsys.persistence.JdbcConnectionProvider;

public class InOutPostgresqlDAO implements InOutDAO {

	private static final Logger logger = Logger.getLogger(InOutPostgresqlDAO.class.getName());
	private final JdbcConnectionProvider cp;
	
	@Inject
	public InOutPostgresqlDAO(JdbcConnectionProvider cp) {
		this.cp = cp;
	}
	
	@PostConstruct
	void init() {
		try {
			Connection con = cp.getConnection();
			try {
				PreparedStatement st = con.prepareStatement("create table if not exists inout ("
						+ "id varchar not null primary key,"
						+ "person_id varchar not null,"
						+ "status varchar not null,"
						+ "created timestamp not null default now(),"
						+ "unique (person_id)");
				try {
					st.execute();
					
				} finally {
					st.close();
				}
			} finally {
				cp.closeConnection(con);
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}	
	
	
	private InOut getInOut(ResultSet rs) throws SQLException {
		String personId = rs.getString("personId");
		Status status = Status.valueOf(rs.getString("status"));
		return new InOut(personId, status);
	}
	
	@Override
	public void persist(InOut io) {
		try {
			Connection con = cp.getConnection();
			try {
				String query = "";
				if (io.getId() != null) {
					query = "update inout set person_id = ?, status = ? where id = ?";
				} else {
					io.setId(UUID.randomUUID().toString());
					query = "insert into inout (person_id, status, id) values (?,?,?)";
				}
				
				PreparedStatement st = con.prepareStatement(query);
				try {
					st.setString(1, io.getPersonId());
					st.setString(2, io.getStatus().toString());
					st.setString(3, io.getId());
					st.executeUpdate();
					
				} finally {
					st.close();
				}
			} finally {
				cp.closeConnection(con);
			}
			
		} catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	@Override
	public InOut findBy(String personId) {
		try {
			Connection con = cp.getConnection();
			try {
				String query = "select * from inout where personId = ?";
				PreparedStatement st = con.prepareStatement(query);
				try {
					st.setString(1,personId);
					ResultSet rs = st.executeQuery();
					try {
						if (rs.next()) {
							return getInOut(rs);
						} else {
							return null;
						}
						
					} finally {
						rs.close();
					}
					
				} finally {
					st.close();
				}
				
			} finally {
				con.close();
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return null;
		}
	}

}
