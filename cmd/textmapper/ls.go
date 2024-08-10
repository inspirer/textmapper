package main

import (
	"context"
	"log"
	"os"

	"github.com/inspirer/textmapper/ls"
	"go.lsp.dev/jsonrpc2"
	lsp "go.lsp.dev/protocol"
	"go.uber.org/zap"
)

var lsCmd = &command{
	Name:  "ls",
	Title: "start a language server",
	Help: `This command starts a language server communicating with Visual Studio
Code (or similar editors) via stdin/stdout and providing coding
assistance for Textmapper grammars.
`,
}

func init() {
	lsCmd.Run = startLS
}

func startLS(ctx context.Context, files []string) error {
	logger, err := zap.NewDevelopment()
	if err != nil {
		log.Fatalln(err.Error())
	}
	logger.Info("textmapper LS is starting ..")

	server := ls.NewServer(logger)

	conn := jsonrpc2.NewConn(jsonrpc2.NewStream(transport{logger}))
	client := lsp.ClientDispatcher(conn, logger.Named("client"))
	ctx = lsp.WithClient(ctx, client)

	server.SetClient(client)

	conn.Go(ctx,
		lsp.Handlers(
			lsp.ServerHandler(server, jsonrpc2.MethodNotFoundHandler),
		),
	)

	logger.Info("listening ..")
	<-conn.Done()
	logger.Info("done")
	return nil
}

type transport struct {
	*zap.Logger
}

func (transport) Read(p []byte) (int, error) {
	return os.Stdin.Read(p)
}

func (transport) Write(p []byte) (int, error) {
	return os.Stdout.Write(p)
}

func (t transport) Close() error {
	t.Logger.Info("closing connections ..")
	if err := os.Stdin.Close(); err != nil {
		t.Logger.Sugar().Errorf("cannot close stdin: %v", err)
		return err
	}
	if err := os.Stdout.Close(); err != nil {
		t.Logger.Sugar().Errorf("cannot close stdout: %v", err)
		return err
	}
	return nil
}
