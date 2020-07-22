/*
 * MIT License
 *
 * Copyright (c) 2020 manager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import MainLayout from "../../../views/mainLayout/MainLayout";
import Tabs, {Tab} from "../../../components/tabs/Tabs";
import React from "react";
import SshCommand, {ISshCommand} from "./SshCommand";
import SshFile, {ISshFile} from "./SshFile";
import styles from "./Ssh.module.css";
import {Resizable} from "re-resizable";
import ScrollBar from "react-perfect-scrollbar";
import {ReduxState} from "../../../reducers";
import {connect} from "react-redux";

interface StateToProps {
  sidenavVisible: boolean;
}

type Props = StateToProps;

interface ICommand extends ISshCommand {
  timestamp: number;
}

interface IFileTransfer extends ISshFile {
  timestamp: number;
}

interface State {
  commands: (ICommand | IFileTransfer)[];
  commandsHeight: number;
  animate: boolean;
}

class Ssh extends React.Component<Props, State> {

  private commandsContainer: any = null;
  private commandsScrollbar: ScrollBar | null = null;
  private leftControlsScrollbar: ScrollBar | null = null;
  private rightControlsScrollbar: ScrollBar | null = null;
  private COMMANDS_MIN_HEIGHT = 44;
  private COMMANDS_DEFAULT_HEIGHT = 175;

  constructor(props: Props) {
    super(props);
    this.state = {
      commands: [],
      commandsHeight: this.COMMANDS_DEFAULT_HEIGHT,
      animate: false,
    }
  }

  private static pad(n: number, width: number, padWith = 0) {
    return (String(padWith).repeat(width) + String(n)).slice(String(n).length);
  }

  componentDidMount() {
    this.updateScrollbars();
  }

  componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any) {
    if (!this.state.animate && prevProps.sidenavVisible !== this.props.sidenavVisible) {
      this.setState({animate: true});
    }
  }

  public render() {
    const body = document.body, html = document.documentElement;
    const maxHeight = Math.max(body.scrollHeight, body.offsetHeight,
      html.clientHeight, html.scrollHeight, html.offsetHeight) - 50 - 56; // 56 for header and 50 for footer
    return (
      <MainLayout>
        <div className={`container ${styles.tabsContainer}`}>
          <Tabs tabs={this.tabs()}/>
        </div>
        <Resizable
          className={`${styles.commandsContainer} ${this.state.animate ? (this.props.sidenavVisible ? styles.shrink : styles.expand) : ''}`}
          maxHeight={maxHeight}
          onResize={this.onResize}
          enable={{top: true}}
          size={{
            width: this.props.sidenavVisible ? window.innerWidth - 200 : '100%',
            height: this.state.commandsHeight
          }}
          onResizeStop={(e, direction, ref, d) => {
            this.setState({
              commandsHeight: this.state.commandsHeight + d.height,
            });
          }}>
          <div className={styles.controlsMenuLeft}>
            <ScrollBar ref={(ref) => {
              this.leftControlsScrollbar = ref;
            }}>
              <button className='btn-floating btn-flat btn-small tooltipped'
                      onClick={this.clearCommands}
                      data-position={'right'}
                      data-tooltip={'Clear'}>
                <i className="material-icons grey-text">delete_sweep</i>
              </button>
            </ScrollBar>
          </div>
          <ScrollBar ref={(ref) => {
            this.commandsScrollbar = ref;
          }} style={{flexGrow: 1}}
                     containerRef={(container) => {
                       this.commandsContainer = container;
                     }}>
            <div>
              <div className={styles.commandsHeader}>
                <div className={styles.commandsTitle}>
                  Commands
                </div>
              </div>
            </div>
            <div className={styles.commands}>
              {this.state.commands.map((command, index) => (
                <div key={index}>
                  {'output' in command ?
                    <>
                      <div>
                                                <span
                                                  className={styles.time}>{this.timestampToString(command.timestamp)}</span>
                        <span className={styles.hostname}>{command.hostname} ></span>
                        <span className={styles.command}>{command.command}</span>
                        {command.exitStatus !== 0 && <span
                            className={styles.exitStatus}>(exit: {command.exitStatus})</span>}
                      </div>
                      <div dangerouslySetInnerHTML={{
                        __html:
                          (command.exitStatus === 0 ? command.output.join("\n") : command.error.join("\n"))
                            .replace(/(?:\r\n|\r|\n)/g, '<br/>')
                      }}/>
                    </>
                    :
                    <>
                      <div>
                                                <span
                                                  className={styles.time}>{this.timestampToString(command.timestamp)}</span>
                        File {command.filename} transferred to {command.hostname}
                      </div>
                    </>
                  }
                </div>
              ))}
            </div>
          </ScrollBar>
          <div className={styles.controlsMenuRight}>
            <ScrollBar ref={(ref) => {
              this.rightControlsScrollbar = ref;
            }}>
              <button className={`btn-floating btn-flat btn-small tooltipped`}
                      onClick={this.toggleCommands}
                      data-position={'left'}
                      data-tooltip={this.state.commandsHeight <= this.COMMANDS_MIN_HEIGHT ? 'Show' : 'Hide'}>
                <i
                  className="material-icons">{this.state.commandsHeight <= this.COMMANDS_MIN_HEIGHT ? 'keyboard_arrow_up' : 'keyboard_arrow_down'}</i>
              </button>
            </ScrollBar>
          </div>
        </Resizable>
      </MainLayout>
    );
  }

  private tabs = (): Tab[] => [
    {
      title: 'Execute command',
      id: 'executeCommand',
      content: () => <SshCommand onExecuteCommand={this.addCommand}/>
    },
    {
      title: 'Upload file',
      id: 'uploadFile',
      content: () => <SshFile onTransferFile={this.addFileTransfer}/>
    }
  ];

  private onResize = () =>
    this.updateScrollbars();

  private updateScrollbars = () => {
    this.commandsScrollbar?.updateScroll();
    this.leftControlsScrollbar?.updateScroll();
    this.rightControlsScrollbar?.updateScroll();
  }

  private toggleCommands = () =>
    this.setState({commandsHeight: this.state.commandsHeight <= this.COMMANDS_MIN_HEIGHT ? this.COMMANDS_DEFAULT_HEIGHT : this.COMMANDS_MIN_HEIGHT},
      () => setTimeout(() => this.updateScrollbars(), 500));

  private clearCommands = () =>
    this.setState({commands: []});

  private addCommand = (sshCommand: ISshCommand) => {
    const command = {...sshCommand, timestamp: Date.now()};
    this.setState({commands: this.state.commands.concat(command)}, () => {
      this.commandsContainer.scrollTop = Number.MAX_SAFE_INTEGER;
    });
  }

  private addFileTransfer = (fileTransfer: ISshFile) => {
    const transfer = {...fileTransfer, timestamp: Date.now()};
    this.setState({commands: this.state.commands.concat(transfer)}, () => {
      this.commandsContainer.scrollTop = Number.MAX_SAFE_INTEGER;
    });
  }

  private timestampToString = (timestamp: number): string => {
    const date = new Date(timestamp);
    let millis = Ssh.pad(date.getMilliseconds(), 3);
    return `${date.toLocaleTimeString()}:${millis}`
  }
}

const mapStateToProps = (state: ReduxState): StateToProps => (
  {
    sidenavVisible: state.ui.sidenav.user && state.ui.sidenav.width,
  }
);

export default connect(mapStateToProps)(Ssh);