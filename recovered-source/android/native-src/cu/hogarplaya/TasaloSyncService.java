package cu.hogarplaya;
import android.app.job.JobService;
import android.app.job.JobParameters;
public final class TasaloSyncService extends JobService {
    public boolean onStartJob(JobParameters params){TasaloReference.NETWORK.execute(()->{boolean retry=TasaloReference.sync(getApplicationContext());jobFinished(params,retry);SyncEngine.changed();});return true;}
    public boolean onStopJob(JobParameters params){return true;}
}
