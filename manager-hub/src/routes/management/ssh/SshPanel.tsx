import styles from "./Ssh.module.css";
import ScrollBar from "react-perfect-scrollbar";
import {Resizable} from "re-resizable";
import React from "react";
import {ReduxState} from "../../../reducers";
import {addCommand, clearCommands} from "../../../actions";
import {connect} from "react-redux";
import {ISshCommand} from "./SshCommand";
import {ISshFile} from "./SshFile";
import {IHostAddress} from "../hosts/Hosts";

export interface ICommand extends ISshCommand {
    timestamp: number;
}

export interface IFileTransfer extends ISshFile {
    timestamp: number;
}

interface SshPanelStateToProps {
    sidenavVisible: boolean;
    commands: (ICommand | IFileTransfer)[];
}

interface SshPanelDispatchToProps {
    addCommand: (command: ICommand | IFileTransfer) => void;
    clearCommands: (hostAddress?: IHostAddress) => void;
}

interface Props {
    filter?: (command: ICommand | IFileTransfer) => boolean;
    hostAddress?: IHostAddress;
}

export type SshPanelProps = Props & SshPanelStateToProps & SshPanelDispatchToProps;

interface State {
    commandsHeight: number;
    animate: boolean;
    panelWidth: string;
}

class SshPanel extends React.Component<SshPanelProps, State> implements SshPanel {

    private commandsContainer: any = null;
    private commandsScrollbar: ScrollBar | null = null;
    private leftControlsScrollbar: ScrollBar | null = null;
    private rightControlsScrollbar: ScrollBar | null = null;
    private COMMANDS_MIN_HEIGHT = 44;
    private COMMANDS_DEFAULT_HEIGHT = 175;

    constructor(props: SshPanelProps) {
        super(props);
        this.state = {
            commandsHeight: this.COMMANDS_DEFAULT_HEIGHT,
            animate: false,
            panelWidth: this.props.sidenavVisible ? 'calc(100vw - 225px)' : '100vw'
        }
    }

    public componentDidMount() {
        this.updateScrollbars();
        window.addEventListener('resize', this.handleResize);
    }

    public componentDidUpdate(prevProps: Readonly<SshPanelProps>, prevState: Readonly<State>, snapshot?: any) {
        if (!this.state.animate && prevProps.sidenavVisible !== this.props.sidenavVisible) {
            this.setState({animate: true});
        }
    }

    public componentWillUnmount() {
        window.removeEventListener('resize', this.handleResize);
    }

    private handleResize = () => {
        this.setState({
            panelWidth: this.props.sidenavVisible ? 'calc(100vw - 225px)' : '100vw'
        })
    }

    public scrollToTop = () =>
        this.commandsContainer.scrollTop = Number.MAX_SAFE_INTEGER;

    private filteredCommands = () => {
        const {commands, filter} = this.props;
        return !filter ? commands : commands.filter((command: ICommand | IFileTransfer) => filter(command));
    }

    private clearCommands = () => {
        this.props.clearCommands(this.props.hostAddress);
    }

    public render() {
        const body = document.body, html = document.documentElement;
        const maxHeight = Math.max(body.scrollHeight, body.offsetHeight,
            html.clientHeight, html.scrollHeight, html.offsetHeight) - 50 - 56; // 56 for header and 50 for footer
        return <Resizable
            className={`${styles.commandsContainer} ${this.state.animate ? (this.props.sidenavVisible ? styles.shrink : styles.expand) : ''}`}
            style={{
                width: this.state.panelWidth,
                left: this.props.sidenavVisible ? '225px' : '0'
            }}
            maxHeight={maxHeight}
            onResize={this.onResize}
            enable={{top: true}}
            size={{
                width: this.props.sidenavVisible ? window.innerWidth - 225 : '100vw',
                height: this.state.commandsHeight,
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
                    <button className='btn-floating btn-flat btn-small'
                            data-for='tooltip' data-tip="Limpar" data-place='top'
                            onClick={this.clearCommands}>
                        <i className='material-icons grey-text'>delete_sweep</i>
                    </button>
                </ScrollBar>
            </div>
            <ScrollBar ref={(ref) => this.commandsScrollbar = ref}
                       style={{flexGrow: 1}}
                       containerRef={(container) => {
                           this.commandsContainer = container;
                       }}>
                <div>
                    <div className={styles.commandsHeader}>
                        <div className={styles.commandsTitle}>
                            Comandos
                        </div>
                    </div>
                </div>
                <div className={styles.commands}>
                    {this.filteredCommands().map((command: ICommand | IFileTransfer, index: number) => (
                        <div key={index}>
                            {'output' in command ?
                                <>
                                    <div>
                                        <span className={styles.time}>{this.timestampToString(command.timestamp)}</span>
                                        {!this.props.filter &&
                                        <span className={styles.hostname}>{`${command.hostAddress.publicIpAddress}${command.hostAddress.privateIpAddress ? '/' + command.hostAddress.privateIpAddress : ''}:`}</span>}
                                        <span className={styles.command}>{command.command}</span>
                                        {command.exitStatus !== 0 && command.error !== null && command.output !== null &&
                                        <span className={styles.exitStatus}>(exit: {command.exitStatus})</span>}
                                    </div>
                                    {command.error !== null && command.output !== null &&
                                    <div className={styles.commandResult} dangerouslySetInnerHTML={{
                                        __html:
                                            ((command.exitStatus === 0 && command.error.length && command.error[0] === "") ? command.output.join("\n") : command.error.join("\n"))
                                                .replace(/(?:\r\n|\r|\n)/g, '<br/>')
                                    }}/>}
                                </>
                                :
                                <>
                                    <div>
                                        <span className={styles.time}>{this.timestampToString(command.timestamp)}</span>
                                        O ficheiro {command.filename} foi transferido
                                        para o host {`${command.hostAddress.publicIpAddress}${command.hostAddress.privateIpAddress ? '/' + command.hostAddress.privateIpAddress : ''} com sucesso`}
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
                    <button className={`btn-floating btn-flat btn-small`}
                            data-for='tooltip' data-tip={this.state.commandsHeight <= this.COMMANDS_MIN_HEIGHT ? 'Expandir' : 'Esconder'}
                            data-place='left'
                            onClick={this.toggleCommands}>
                        <i className='material-icons'>{this.state.commandsHeight <= this.COMMANDS_MIN_HEIGHT ? 'keyboard_arrow_up' : 'keyboard_arrow_down'}</i>
                    </button>
                </ScrollBar>
            </div>
        </Resizable>;
    }

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

    private timestampToString = (timestamp: number): string => {
        const date = new Date(timestamp);
        return this.dateFormat(date);
    }

    private dateFormat = (date: Date) => {
        const monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
        const day = date.getDate();
        const monthIndex = date.getMonth();
        const monthName = monthNames[monthIndex];
        const year = date.getFullYear().toString().substr(-2);
        const millis = this.pad(date.getMilliseconds(), 3);
        return `${day}/${monthName}/${year} ${date.toLocaleTimeString()}.${millis}`;
    }

    private pad = (n: number, width: number, padWith = 0) =>
        (String(padWith).repeat(width) + String(n)).slice(String(n).length);
}

const mapStateToProps = (state: ReduxState): SshPanelStateToProps => (
    {
        sidenavVisible: state.ui.sidenav.user && state.ui.sidenav.width,
        commands: state.entities.commands
    }
);

const mapDispatchToProps: SshPanelDispatchToProps = {
    addCommand,
    clearCommands,
};

export default connect(mapStateToProps, mapDispatchToProps)(SshPanel);