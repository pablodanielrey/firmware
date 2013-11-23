package ar.com.dcsys.firmware.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import ar.com.dcsys.data.HsqlConnectionProvider;
import ar.com.dcsys.data.fingerprint.Fingerprint;
import ar.com.dcsys.exceptions.FingerprintException;
import ar.com.dcsys.firmware.exceptions.DatabaseException;

public class FingerprintReaderMappingHsqlDAO implements FingerprintReaderMappingDAO {

	private final Logger logger = Logger.getLogger(FingerprintReaderMappingHsqlDAO.class.getName());
	private final HsqlConnectionProvider cp;
	private final Params params;
	
	@Inject
	public FingerprintReaderMappingHsqlDAO(HsqlConnectionProvider cp, Params params) {
		this.cp = cp;
		this.params = params;
	}

	@PostConstruct
	public void init() {
		try {
			createTables();
		} catch (SQLException e) {
			
		}
	}
	
	/**
	 * Crea las tablas dentro de la base.
	 */
	public void createTables() throws SQLException {
		Connection con = cp.getConnection();
		try {
			PreparedStatement st = con.prepareStatement("create table if not exists fingerprintsreader (" +
					"id bigint not null primary key, " +
					"fingerprint_id longvarchar not null)");
			try {
				st.executeUpdate();
				
			} finally {
				st.close();
			}
		} finally {
			con.close();
		}
	}
	
	
	@Override
	public FingerprintReaderMapping findById(Long id) throws DatabaseException {
		try {
			Connection con = cp.getConnection();
			try {
				String query = "select * from fingerprintsreader where id = ?";
				PreparedStatement st = con.prepareStatement(query);
				try {
					st.setLong(1,id);
					ResultSet rs = st.executeQuery();
					try {
						if (rs.next()) {
							FingerprintReaderMapping fp = getFingerPrint(rs);
							return fp;
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
				cp.closeConnection(con);
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	
	private boolean delete(Connection con, FingerprintReaderMapping fprm) throws SQLException {
		String query = "delete from fingerprintsreader where id = ?";
		PreparedStatement st = con.prepareStatement(query);
		try {
			st.setLong(1,fprm.getId());
			return st.execute();
		} finally {
			st.close();
		}
	}
	
	private boolean insert(Connection con, FingerprintReaderMapping fprm) throws SQLException {
		String query = "insert into fingerprintsreader (id,fingerprint_id) values (?,?)";
		PreparedStatement st = con.prepareStatement(query);
		try {
			st.setLong(1,fprm.getId());
			st.setString(2,fprm.getFingerprintId());
			return st.execute();
		} finally {
			st.close();
		}
	}
	
	
	@Override
	public void persist(FingerprintReaderMapping fprm) throws DatabaseException {
		if (fprm.getId() == null) {
			throw new DatabaseException("fingerprint.id == null");
		}
		
		try {
			Connection con = cp.getConnection();
			try {
				if (delete(con, fprm)) {
					if (insert(con, fprm)) {
						return;
					}
				}
			} finally {
				cp.closeConnection(con);
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}		
		
		throw new DatabaseException();
	}
	
	
	
	private FingerprintReaderMapping getFingerPrint(ResultSet rs) throws SQLException {
		String id = rs.getString("id");
		String fingerprintId = rs.getString("fingerprint_id");
		
		FingerprintReaderMapping fprm = new FingerprintReaderMapping();
		fprm.setFingerprintId(fingerprintId);
		fprm.setId(Long.valueOf(id));
		
		return fprm;
	}
	
}
