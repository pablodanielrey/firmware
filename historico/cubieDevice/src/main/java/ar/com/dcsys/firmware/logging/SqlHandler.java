package ar.com.dcsys.firmware.logging;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import ar.com.dcsys.persistence.JdbcConnectionProvider;

public class SqlHandler extends Handler {

	private static final Logger logger = Logger.getLogger(SqlHandler.class.getName());
	private JdbcConnectionProvider cp;
	
	@Inject
	public SqlHandler(JdbcConnectionProvider cp) {
		this.cp = cp;
	}
	
	@PostConstruct
	private void init() {
		try {
			Connection con = cp.getConnection();
			try {
				PreparedStatement st = con.prepareStatement("create table if not exists logs "
																		+ "(created timestamp not null default now(),"
																		+ "source varchar not null,"
																		+ "log varchar not null)");
				try {
					st.execute();
				} finally {
					st.close();
				}
			} finally {
				con.close();
			}

		} catch (SQLException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
		}
	}
	
	@Override
	public void close() throws SecurityException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void flush() {
	}

	@Override
	public void publish(LogRecord l) {
		
		try {
			Connection con = cp.getConnection();
			try {
				PreparedStatement addLog = con.prepareStatement("insert into logs (log,source) values (?,?)");
				try {
					addLog.setString(1, l.getMessage());
					addLog.setString(2, l.getSourceClassName() + " " + l.getSourceMethodName());
					addLog.execute();
					
				} finally {
					addLog.close();
				}
				
			} finally {
				con.close();
			}
			
		} catch (Exception e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
		}
	}


}
