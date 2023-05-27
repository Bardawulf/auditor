<template>
  <v-container id="regular-tables" fluid tag="section">
    <v-row justify="space-between">
      <v-col cols="6" md="2">
        <v-select v-model="pageSize" :items="pageSizeOptions" prepend-icon="mdi-format-align-justify" menu-props="auto"
          hide-details label="pageSize" single-line>
        </v-select>
      </v-col>
      <v-spacer />
      <!-- <v-col cols="6" md="2">
      <v-btn
        color="success"
        @click="loggedin ? logout() : loginWindow = true">
        {{loggedin ? 'Log out' : 'Log in'}}
      </v-btn>
    </v-col> -->
      <v-col cols="6" md="2">
        <router-link :to="{ name: 'Mail', query: { action: 'create' } }">
          <v-btn color="success">
            Create new
          </v-btn>
        </router-link>
      </v-col>
    </v-row>

    <base-material-card icon="mdi-email-multiple-outline" title="Mail list" class="px-5 py-3">
      <v-simple-table>
        <thead>
          <tr>
            <th class="primary--text display-1">No.</th>
            <th class="primary--text display-1" @click="sort('name')">Name</th>
            <th class="primary--text display-1" @click="sort('text')">Text</th>
            <th></th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="(mail, i) in sortedMails" :key="mail.id">
            <td>{{ (i + 1) * currentPage }}</td>
            <td>{{ mail.topic }}</td>
            <td>
              <div v-if="mail.body.length < 80"> {{ mail.body }}</div>
              <div v-else> {{ mail.body.substring(0, 80) + '...' }}</div>
            </td>
            <td class="text-right">
              <router-link tag="button" :to="{ name: 'Mail', query: { action: 'edit', id: mail.id } }">
                <v-tooltip open-delay="83" bottom>
                  <template v-slot:activator="{ on, attrs }">
                    <v-icon v-bind="attrs" v-on="on" class="mx-1">
                      mdi-email-edit-outline
                    </v-icon>
                  </template>
                  <span>Edit</span>
                </v-tooltip>
              </router-link>
              <v-tooltip open-delay="83" bottom>
                <template v-slot:activator="{ on, attrs }">
                  <v-icon @click="deleteMail(mail.id)" v-bind="attrs" v-on="on" color="error" class="mx-1">
                    mdi-delete
                  </v-icon>
                </template>
                <span>Delete</span>
              </v-tooltip>
            </td>
          </tr>
        </tbody>
      </v-simple-table>
      <v-row>
        <v-col>
          <v-btn color="success" v-if='prev' @click="prevPage">Previous</v-btn>
        </v-col>
        <v-col cols="1">
          <v-btn color="success" v-if='next' @click="nextPage">Next</v-btn>
        </v-col>
      </v-row>
    </base-material-card>
    <v-dialog v-model="loginWindow" max-width="300">
      <v-card>
        <v-card-title class="primary--text display-2">
          Login
          <v-spacer />

          <v-icon aria-label="Close" @click="loginWindow = false, userMail = '', userPassword = ''">
            mdi-close
          </v-icon>
        </v-card-title>

        <v-card-text>
          <v-row>
            <v-text-field v-model="userLogin" label="Mail" hint="example@example.com" outlined clearable single-line />
            <v-text-field v-model="userPassword" :append-icon="show1 ? 'mdi-eye' : 'mdi-eye-off'"
              :type="show1 ? 'text' : 'password'" label="Password" @click:append="show1 = !show1" outlined clearable
              single-line />
          </v-row>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" text @click="login(), loginWindow = false">
            Ok
          </v-btn>
          <v-btn color="primary" text @click="loginWindow = false, userMail = '', userPassword = ''">
            Close
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script>
import { get, post, del } from '../../helpers/api'

export default {
  name: 'MailsList',

  data: () => ({
    currentSort: 'Name',
    currentSortDir: 1,
    pageSize: 5,
    pageSizeOptions: [5, 10, 15, 20, 25, 30],
    currentPage: 1,
    prev: false,
    next: true,
    mails: [],
    loggedin: true,
    loginWindow: false,
    userLogin: '',
    userPassword: '',
    show1: false
  }),

  computed: {
    sortedMails() {
      return this.mails.slice().sort((a, b) => {
        if (a[this.currentSort] < b[this.currentSort]) return -1 * this.currentSortDir;
        if (a[this.currentSort] > b[this.currentSort]) return this.currentSortDir;
        return 0;
      }).filter((row, index) => {
        let start = (this.currentPage - 1) * this.pageSize;
        let end = this.currentPage * this.pageSize;
        if (index >= start && index < end) return true;
      });
    },
  },

  watch: {
    pageSize() {
      this.next = ((this.currentPage * this.pageSize) < this.mails.length) ? true : false;
      this.prev = (this.currentPage == 1) ? false : true;
    },
    currentPage() {
      this.next = ((this.currentPage * this.pageSize) < this.students.length) ? true : false;
      this.prev = (this.currentPage == 1) ? false : true;
    },
  },

  methods: {
    // get information
    getMails() {
      get(this, '/template/all', '', response => {
        this.mails = response.data;
      })
    },
    // sorting
    sort(col) {
      if (this.currentSort === col) {
        this.currentSortDir = this.currentSortDir === 1 ? -1 : 1;
      } else {
        this.currentSort = col;
      }
    },
    // navigation
    nextPage() {
      if ((this.currentPage * this.pageSize) < this.mails.length)
        this.currentPage++;
    },
    prevPage() {
      if (this.currentPage > 1)
        this.currentPage--;
    },
    // remove
    deleteMail(id) {
      del(this, '/template/' + id, '', () => {
        this.tableInfo = '';
        this.mails.splice(this.mails.indexOf(this.mails.find(s => s.id === id)), 1);
        this.$store.dispatch('setSnackbar', { text: "Success" });
      }, error => {
        this.$store.dispatch('setSnackbar', { text: error, color: "error" });
      });
    },
    login() {
      let data = {
        userMail: this.userLogin,
        password: this.userPassword
      }

      post(this, '/mail/register', data, response => {
        this.$store.dispatch('setSnackbar', { text: response.data });
        this.userLogin = '';
        this.userPassword = '';
        this.loggedin = true;
      }, error => {
        this.$store.dispatch('setSnackbar', { text: error, color: "error" });
      });
    },
    logout() {
      post(this, '/mail/logout', '', () => {
        this.$store.dispatch('setSnackbar', { text: "Success" });
        this.loggedin = false;
      }, error => {
        this.$store.dispatch('setSnackbar', { text: error, color: "error" });
      });
    }
  },
  created() {
    this.getMails();
    this.next = ((this.currentPage * this.pageSize) < this.mails.length) ? true : false;
  },
}
</script>