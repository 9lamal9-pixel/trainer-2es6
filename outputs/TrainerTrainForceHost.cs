using System;
using System.Windows.Forms;

partial class TrainerIntegrationHost {
    readonly TrainForceHistory trainForceHistory = new TrainForceHistory();
    TrainForceForm trainForceView;
    long trainForceEpoch;

    void InstallTrainForceMode() {
        FormClosed += delegate { CloseTrainForceMode(); };
    }
    void OpenTrainForceMode() {
        using (TrainerManagedFloatingScope.Enter()) {
            if (trainForceView == null || trainForceView.IsDisposed) {
                trainForceView = new TrainForceForm();
                foreach (var sample in trainForceHistory.ToArray()) trainForceView.Push(sample);
            }
            trainForceView.Show();
            trainForceView.Activate();
        }
    }
    void CaptureTrainForces() {
        // Called inside the scoped managed publication, after a reset or an
        // accepted physics frame. Capture is read-only and never calls native.
        if (managedMotion == null) return;
        var sample = TrainForceSample.Capture(managedMotion, trainForceEpoch);
        trainForceHistory.Add(sample);
        if (trainForceView != null && !trainForceView.IsDisposed) trainForceView.Push(sample);
    }
    void CloseTrainForceMode() {
        if (trainForceView != null) { trainForceView.Dispose(); trainForceView = null; }
        trainForceHistory.Clear();
    }
}
