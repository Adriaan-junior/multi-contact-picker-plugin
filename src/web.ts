import { WebPlugin } from '@capacitor/core';
import { ContactPluginPlugin } from './definitions';

export class ContactPluginWeb extends WebPlugin implements ContactPluginPlugin {
  constructor() {
    super({
      name: 'ContactPlugin',
      platforms: ['web'],
    });
  }

  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  async getContacts(): Promise<{ results: any[] }> {
    return {
      results: [{
        id: '1',
        displayName: 'IT Works',
        phoneNumber: '123456789'
      },
      {
        id: '2',
        displayName: 'User 2',
        phoneNumber: '+27568746320'
      },]
    };
  }

  async search(number: string): Promise<{ results: any[] }> {
    console.log('number == ', number);
    return {
      results: [{
        id: '1',
        displayName: 'IT Works',
        phoneNumber: '123456789'
      },
      {
        id: '2',
        displayName: 'User 2',
        phoneNumber: '+27568746320'
      },]
    };
  }
}

const ContactPlugin = new ContactPluginWeb();

export { ContactPlugin };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(ContactPlugin);
