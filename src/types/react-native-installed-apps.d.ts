declare module 'react-native-installed-apps' {
  interface AppInfo {
    appName: string;
    packageName: string;
    versionName?: string;
  }

  const InstalledApps: {
    getApps(): Promise<AppInfo[]>;
  };

  export default InstalledApps;
} 