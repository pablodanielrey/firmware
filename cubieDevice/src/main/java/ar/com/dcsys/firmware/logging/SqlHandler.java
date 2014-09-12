package ar.com.dcsys.firmware.logging;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import ar.com.dcsys.persistence.JdbcConnectionProvider;

public class SqlHandler extends Handler {

	private static final Logger logger = Logger.getLogger(SqlHandler.class.getName());
	private JdbcConnectionProvider cp;
	private Connection con;
	private PreparedStatement addLog;
	
	@Inject
	public SqlHandler(JdbcConnectionProvider cp) {
		this.cp = cp;
	}

	@PostConstruct
	private void init() {
		try {
			this.con = cp.getConnection();
			PreparedStatement st = this.con.prepareStatement("create table if not exists logs "
																	+ "(created timestamp not null default now(),"
																	+ "source varchar not null,"
																	+ "log varchar not null)");
			try {
				st.execute();
			} finally {
				st.close();
			}

			addLog = con.prepareStatement("insert into logs (log,source) values (?,?)");
			
		} catch (SQLException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
		}
	}
	
	@PreDestroy
	@Override
	public void close() throws SecurityException {

		try {
			if (addLog != null) {
				addLog.close();
				addLog = null;
			}
		} catch (SQLException e) {
			throw new SecurityException(e.getMessage());
		}

		try {
			if (con != null) {
				con.close();	
				con = null;
			}
		} catch (SQLException e) {
			throw new SecurityException(e.getMessage());
		}
	}

	@Override
	public void flush() {
	}

	@Override
	public void publish(LogRecord l) {
		try {
			addLog.setString(1, l.getMessage());
			addLog.setString(2, l.getSourceClassName() + " " + l.getSourceMethodName());
			addLog.execute();
			
		} catch (Exception e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
		}
	}


}
