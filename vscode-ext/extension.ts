import { ExtensionContext, workspace } from 'vscode';

import {
	Executable,
	LanguageClient,
	LanguageClientOptions,
} from 'vscode-languageclient/node';

let client: LanguageClient;

export async function activate(context: ExtensionContext) {
	const config = workspace.getConfiguration('textmapper');
	const serverOptions: Executable = {
		command: config.get("path"),
		args: ["ls"],
	};
	const clientOptions: LanguageClientOptions = {
		documentSelector: [{ scheme: 'file', language: 'textmapper' }],
		diagnosticCollectionName: "textmapper",
	};

	const client = new LanguageClient(
		'tm-lsp',
		'Textmapper',
		serverOptions,
		clientOptions
	);

	await client.start();
	context.subscriptions.push(client);
}

export async function deactivate() {
	if (client) {
		await client.stop();
	}
}