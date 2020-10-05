/********************************************************************************
 * Copyright (c) 2019-2020 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
import { ILogger } from "@theia/core";
import { BackendApplicationContribution } from "@theia/core/lib/node/backend-application";
import { RawProcess, RawProcessFactory } from "@theia/process/lib/node/raw-process";
import { Application } from "express";
import { inject, injectable } from "inversify";
import os = require("os");
import * as path from "path";

import { FileGenServer } from "../common/generate-protocol";

@injectable()
export class EcoreFileGenServer implements FileGenServer, BackendApplicationContribution {

    constructor(
        @inject(RawProcessFactory) protected readonly processFactory: RawProcessFactory,
        @inject(ILogger) private readonly logger: ILogger) { }

    generateEcore(name: string, prefix: string, uri: string, workspacePath: string): Promise<string> {
        const jarPath = this.getEclipseProductJar();

        const command = "java";
        const args: string[] = [];
        let platformWorkspacePath = workspacePath;
        if (os.platform() === "win32") {
            platformWorkspacePath = workspacePath.substr(1);
        }
        args.push(
            "-cp", jarPath,
            "org.eclipse.equinox.launcher.Main",
            "-application", "org.eclipse.emfcloud.ecore.backend.app.create-ecore",
            name, prefix, uri, platformWorkspacePath
        );

        return new Promise(resolve => {
            const process = this.spawnProcess(command, args);
            // eslint-disable-next-line no-null/no-null
            if (process === undefined || process.process === undefined || process === null || process.process === null) {
                resolve("Process not spawned");
                return;
            }

            process.process.on("exit", (code: any) => {
                switch (code) {
                    case 0: resolve("OK"); break;
                    case -10: resolve("Name is missing"); break;
                    case -11: resolve("Prefix is missing"); break;
                    case -12: resolve("Uri is missing"); break;
                    case -13: resolve("Workspace Path is missing"); break;
                    default: resolve("UNKNOWN ERROR"); break;
                }
            });
        });
    }

    generateGenModel(workspacePath: string, ecorePath: string, customPackageName: string, folderName: string): Promise<string> {
        const jarPath = this.getEclipseProductJar();

        const command = "java";
        const args: string[] = [];
        args.push(
            "-cp", jarPath,
            "org.eclipse.equinox.launcher.Main",
            "-application", "org.eclipse.emfcloud.ecore.backend.app.create-genmodel",
            ecorePath, customPackageName, folderName
        );

        return new Promise(resolve => {
            const process = this.spawnProcess(command, args);
            // eslint-disable-next-line no-null/no-null
            if (process === undefined || process.process === undefined || process === null || process.process === null) {
                resolve("Process not spawned");
                return;
            } else {
                if (process.process.stdout) {
                    process.process.stdout.on("data", function (data) {
                        console.log("stdout: " + data.toString());
                    });
                }
            }

            process.process.on("exit", (code: any) => {
                switch (code) {
                    case 0: resolve("OK"); break;
                    case -10: resolve("Ecore File Path is missing"); break;
                    case -11: resolve("Custom Root Package is missing"); break;
                    case -12: resolve("Outputfolder is missing"); break;
                    default: resolve("UNKNOWN ERROR " + code); break;
                }
            });
        });
    }

    generateCode(genmodelPath: string, workspacePath: string): Promise<string> {
        const jarPath = this.getEclipseProductJar();

        const command = "java";
        const args: string[] = [];
        args.push(
            "-cp", jarPath,
            "org.eclipse.equinox.launcher.Main",
            "-data", workspacePath,
            "-application", "org.eclipse.emfcloud.ecore.backend.app.codegen",
            genmodelPath
        );

        return new Promise(resolve => {
            const process = this.spawnProcess(command, args);
            // eslint-disable-next-line no-null/no-null
            if (process === undefined || process.process === undefined || process === null || process.process === null) {
                resolve("Process not spawned");
                return;
            }

            process.process.on("exit", (code: any) => {
                switch (code) {
                    case 0: resolve("OK"); break;
                    default: resolve("UNKNOWN ERROR " + code); break;
                }
            });
        });
    }

    onStop(app?: Application): void {
        this.dispose();
    }

    dispose(): void {
        // do nothing
    }

    setClient(): void {
        // do nothing
    }

    private getEclipseProductJar(): string {
        const jarPath = path.resolve(__dirname, "..", "..", "..", "..",
            "server", "org.eclipse.emfcloud.ecore.backend-app", "org.eclipse.emfcloud.ecore.codegen.product",
            "target", "products", "org.eclipse.emfcloud.ecore.codegen.product", "linux", "gtk", "x86_64", "plugins",
            "org.eclipse.equinox.launcher_1.5.600.v20191014-2022.jar");
        if (jarPath.length === 0) {
            throw new Error("The eclipse.equinox.launcher is not found. ");
        }

        return jarPath;
    }

    private spawnProcess(command: string, args?: string[]): RawProcess | undefined {
        const rawProcess = this.processFactory({ command, args });
        if (rawProcess.process === undefined) {
            return undefined;
        }
        rawProcess.process.on("error", this.onDidFailSpawnProcess.bind(this));
        const stderr = rawProcess.process.stderr;
        if (stderr) {
            stderr.on("data", this.logError.bind(this));
        }
        return rawProcess;
    }

    protected onDidFailSpawnProcess(error: Error): void {
        this.logger.error(error);
    }

    protected logError(data: string | Buffer): void {
        if (data) {
            this.logger.error(`Ecore Gen: ${data}`);
        }
    }

}
