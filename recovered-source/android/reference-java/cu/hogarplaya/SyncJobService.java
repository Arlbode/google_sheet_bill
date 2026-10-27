package cu.hogarplaya;

import android.app.job.JobParameters;
import android.app.job.JobService;

/* loaded from: classes.dex */
public final class SyncJobService extends JobService {
    /* renamed from: lambda$onStartJob$0$cu-hogarplaya-SyncJobService, reason: not valid java name */
    /* synthetic */ void m7lambda$onStartJob$0$cuhogarplayaSyncJobService(JobParameters jobParameters) {
        boolean sync = SyncEngine.sync(getApplicationContext());
        if (jobParameters.getJobId() == 5102) {
            sync = false;
        }
        jobFinished(jobParameters, sync);
    }

    @Override // android.app.job.JobService
    public boolean onStartJob(final JobParameters jobParameters) {
        SyncEngine.NETWORK.execute(new Runnable() { // from class: cu.hogarplaya.SyncJobService$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                SyncJobService.this.m7lambda$onStartJob$0$cuhogarplayaSyncJobService(jobParameters);
            }
        });
        return true;
    }

    @Override // android.app.job.JobService
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
}
