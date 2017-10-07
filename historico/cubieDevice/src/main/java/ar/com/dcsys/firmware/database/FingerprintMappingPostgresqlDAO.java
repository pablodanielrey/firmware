package ar.com.dcsys.firmware.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import ar.com.dcsys.persistence.JdbcConnectionProvider;

public class FingerprintMappingPostgresqlDAO implements FingerprintMappingDAO {

	private static final Logger logger = Logger.getLogger(FingerprintMappingPostgresqlDAO.class.getName());
	private final JdbcConnectionProvider cp;
	
	@Inject
	public FingerprintMappingPostgresqlDAO(JdbcConnectionProvider cp) {
		this.cp = cp;
	}
	
	
	@PostConstruct
	void init() {
		try {
			Connection con = cp.getConnection();
			try {
				PreparedStatement st = con.prepareStatement("create table if not exists fingerprintmappings ("
						+ "fpNumber integer not null primary key,"
						+ "person_id varchar not null,"
						+ "fingerprint_id varchar not null,"
						+ "created timestamp not null default now(),"
						+ "unique (person_id,fingerprint_id))");
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
	
	@Override
	public void persist(FingerprintMapping fp) throws FingerprintMappingException {
		
		if (fp.getFpNumber() < 0 || fp.getFingerprintId() == null || fp.getPersonId() == null) {
			throw new FingerprintMappingException("fpNumer < 0 || fingerprint.id == null || person.id == null");
		}
		
		try {
			Connection con = cp.getConnection();
			try {
				String query = "";
				int fpNumber = fp.getFpNumber();

				query = "select fpNumber from fingerprintmappings where fpNumber = ?";
				PreparedStatement st = con.prepareStatement(query);
				try {
					st.setInt(1, fpNumber);
					ResultSet rs = st.executeQuery();
					try {
						if (rs.next()) {
							query = "update fingerprintmappings set person_id = ?, fingerprint_id = ? where fpNumber = ?";
						} else {
							query = "insert into fingerprintmappings (person_id, fingerprint_id, fpNumber) values (?,?,?)";
						}
						
					} finally {
						rs.close();
					}
				
				} finally {
					st.close();
				}
				
				st = con.prepareStatement(query);
				try {
					st.setString(1, fp.getPersonId());
					st.setString(2, fp.getFingerprintId());
					st.setInt(3, fp.getFpNumber());
					st.executeUpdate();
					
				} finally {
					st.close();
				}
			} finally {
				cp.closeConnection(con);
			}
			
		} catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new FingerprintMappingException(e.getMessage());
		}		
	}
	
	@Override
	public void deleteAll() throws FingerprintMappingException {
		try {
			Connection con = cp.getConnection();
			try {
				String query = "delete from fingerprintmappings";
				PreparedStatement st = con.prepareStatement(query);
				try {
					st.executeUpdate();

				} finally {
					st.close();
				}
				
			} finally {
				con.close();
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new FingerprintMappingException(e.getMessage());
		}				
	}

	@Override
	public void delete(FingerprintMapping fp) throws FingerprintMappingException {

		try {
			Connection con = cp.getConnection();
			try {
				String query = "delete from fingerprintmappings where fpNumber = ? and person_id = ? and fingerprint_id = ?";
				PreparedStatement st = con.prepareStatement(query);
				try {
					st.setInt(1, fp.getFpNumber());
					st.setString(2,fp.getPersonId());
					st.setString(3,fp.getFingerprintId());
					int rows = st.executeUpdate();
					if (rows != 1) {
						throw new FingerprintMappingException("not found");
					}

				} finally {
					st.close();
				}
				
			} finally {
				con.close();
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new FingerprintMappingException(e.getMessage());
		}		
		
	}
	
	
	private FingerprintMapping getFingerprintMapping(ResultSet rs) throws SQLException {
		
		FingerprintMapping fp = new FingerprintMapping();
		fp.setPersonId(rs.getString("person_id"));
		fp.setFingerprintId(rs.getString("fingerprint_id"));
		fp.setFpNumber(rs.getInt("fpNumber"));
		
		return fp;
		
	}
	
	@Override
	public List<FingerprintMapping> findAll() throws FingerprintMappingException {

		try {
			Connection con = cp.getConnection();
			try {
				String query = "select * from fingerprintmappings";
				PreparedStatement st = con.prepareStatement(query);
				try {
					ResultSet rs = st.executeQuery();
					try {
						List<FingerprintMapping> fps = new ArrayList<>();
						while (rs.next()) {
							FingerprintMapping fp = getFingerprintMapping(rs);
							fps.add(fp);
						}
						return fps;
						
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
			throw new FingerprintMappingException(e.getMessage());
		}		
	}

	@Override
	public FingerprintMapping findBy(int fpNumber) throws FingerprintMappingException {

		try {
			Connection con = cp.getConnection();
			try {
				String query = "select * from fingerprintmappings where fpNumber = ?";
				PreparedStatement st = con.prepareStatement(query);
				try {
					st.setInt(1, fpNumber);
					ResultSet rs = st.executeQuery();
					try {
						if (rs.next()) {
							return getFingerprintMapping(rs);
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
			throw new FingerprintMappingException(e.getMessage());
		}		
	}

	@Override
	public FingerprintMapping fingBy(String personId, String fingerprintId)	throws FingerprintMappingException {
		try {
			Connection con = cp.getConnection();
			try {
				String query = "select * from fingerprintmappings where person_id = ? and fingerprint_id = ?";
				PreparedStatement st = con.prepareStatement(query);
				try {
					st.setString(1, personId);
					st.setString(2, fingerprintId);
					ResultSet rs = st.executeQuery();
					try {
						if (rs.next()) {
							return getFingerprintMapping(rs);
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
			throw new FingerprintMappingException(e.getMessage());
		}		
	}
	
}
