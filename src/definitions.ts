declare module '@capacitor/core' {
  interface PluginRegistry {
    ContactPlugin: ContactPluginPlugin;
  }
}

export interface ContactPluginPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  getContacts(): Promise<{ results: any[] }>;
  search(number: string): Promise<{ results: any[] }>;
}
