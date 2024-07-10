package org.eclipse.winery.lsp.Server.ServerCore.Utils;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.SaveOptions;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.TextDocumentSyncOptions;
import java.util.List;

public class ServerInitUtils {
    private ServerInitUtils() {
    }
    public static TextDocumentSyncOptions getDocumentSyncOption() {
        TextDocumentSyncOptions syncOptions = new TextDocumentSyncOptions();
        SaveOptions saveOptions = new SaveOptions(true);
        // Can use Incremental for diff based approach
        // Can use None and if not set, default is None
        syncOptions.setChange(TextDocumentSyncKind.Full);
        // Client will send open and close notifications
        syncOptions.setOpenClose(true);
        syncOptions.setWillSave(true);
        syncOptions.setWillSaveWaitUntil(true);
        syncOptions.setSave(saveOptions);

        return syncOptions;
    }
    
    public static CompletionOptions getCompletionOptions() {
        CompletionOptions completionOptions = new CompletionOptions();

        List<String> triggerCharacters = List.of(".", ">");

        completionOptions.setResolveProvider(true);
        completionOptions.setTriggerCharacters(triggerCharacters);
        completionOptions.setWorkDoneProgress(true);

        return completionOptions;
    }
}
