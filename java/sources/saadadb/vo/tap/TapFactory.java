package saadadb.vo.tap;

import java.sql.ResultSet;

import tap.AbstractTAPFactory;
import tap.ServiceConnection;
import tap.TAPException;
import tap.db.DBConnection;
import tap.metadata.TAPSchema;
import uws.UWSException;
import uws.service.UWSService;
import uws.service.backup.DefaultUWSBackupManager;
import uws.service.backup.UWSBackupManager;
import adql.parser.QueryChecker;
import adql.translator.ADQLTranslator;

public class TapFactory extends AbstractTAPFactory<ResultSet> {

	protected TapFactory(ServiceConnection<ResultSet> service) throws NullPointerException {
		super(service);

	}

	@Override
	public ADQLTranslator createADQLTranslator() throws TAPException {
		return new SaadaSQLTranslator();
	}

	@Override
	public DBConnection<ResultSet> createDBConnection(final String jobId) throws TAPException {
		return new TapDBConnection(jobId);
	}

	@Override
	public UWSBackupManager createUWSBackupManager(UWSService uws)
			throws TAPException,
			UWSException {
		return new DefaultUWSBackupManager(uws, true);
	}

}
