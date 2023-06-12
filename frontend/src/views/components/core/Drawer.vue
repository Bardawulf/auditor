<template>
  <v-navigation-drawer id="core-navigation-drawer" v-model="drawer" color="#ffffff"
    :dark="barColor !== 'rgba(228, 226, 226, 1), rgba(255, 255, 255, 0.7)'" :expand-on-hover="expandOnHover"
    mobile-break-point="960" app width="260" v-bind="$attrs">
    <template v-slot:img="props">
      <v-img :gradient="`to bottom, ${barColor}`" v-bind="props" />
    </template>

    <v-divider class="mb-1" />

    <v-list dense nav>
      <v-list-item>
        <v-list-item-content>
          <v-list-item-title class="display-2" v-text="profile.title" />
        </v-list-item-content>
      </v-list-item>
    </v-list>

    <v-divider class="mb-2" />

    <v-list expand nav>
      <!-- Style cascading bug  -->
      <!-- https://github.com/vuetifyjs/vuetify/pull/8574 -->
      <div />

      <template v-for="(item, i) in computedItems">
        <base-item-group v-if="item.children" :key="`group-${i}`" :item="item">
        </base-item-group>

        <base-item v-else :key="`item-${i}`" :item="item" />
      </template>

      <div />
    </v-list>

    <template v-slot:append>
      <v-list-item>
        <v-row align="center" no-gutters>
          <v-col cols="auto">
            Dark Mode
          </v-col>

          <v-spacer />

          <v-col cols="auto">
            <v-switch v-model="$vuetify.theme.dark" class="ma-0 pa-0" color="white" hide-details />
          </v-col>
        </v-row>
      </v-list-item>

      <v-list-item @click="logout">
        <v-list-item-icon>
          <v-icon>mdi-logout</v-icon>
        </v-list-item-icon>
        <v-list-item-title>Log Out</v-list-item-title>
      </v-list-item>

    </template>


  </v-navigation-drawer>
</template>

<script>
// Utilities
import { mapState, mapGetters } from 'vuex';

export default {
  name: 'DashboardCoreDrawer',

  props: {
    expandOnHover: {
      type: Boolean,
      default: false,
    },
  },

  data() {
    return {
      items: [
        {
          icon: 'mdi-calendar-multiple',
          title: 'Curriculums',
          to: '/',
          group: 'curriculum'
        },
        {
          icon: 'mdi-account-group',
          title: 'Students',
          to: '/students-list',
          group: 'student'
        },
        {
          icon: 'mdi-email-multiple-outline',
          title: 'Template mails',
          to: '/mails-list',
          group: 'mail'
        },
        {
          icon: 'mdi-help-circle',
          title: 'Manuals',
          to: '/manuals-list'
        }
      ],
    };
  },

  computed: {
    ...mapState(['barColor', 'barImage']),
    ...mapGetters(['isAuthenticated']),
    drawer: {
      get() {
        return this.$store.state.drawer;
      },
      set(val) {
        this.$store.commit('SET_DRAWER', val);
      },
    },
    computedItems() {
      return this.items.map(this.mapItem);
    },
    profile() {
      return {
        avatar: true,
        title: this.$t('avatar'),
      };
    },
  },

  methods: {
    mapItem(item) {
      return {
        ...item,
        children: item.children ? item.children.map(this.mapItem) : undefined,
        title: this.$t(item.title),
      };
    },
    logout() {
      if (this.isAuthenticated) {
        this.$store.dispatch('logout');
      }
      this.$router.push('/auth');
    },

  },
};
</script>

<style lang="sass">
  @import '~vuetify/src/styles/tools/_rtl.sass'

  #core-navigation-drawer
    .v-list-group__header.v-list-item--active:before
      opacity: .24

    .v-list-item
      &__icon--text,
      &__icon:first-child
        justify-content: center
        text-align: center
        width: 20px

        +ltr()
          margin-right: 24px
          margin-left: 12px !important

        +rtl()
          margin-left: 24px
          margin-right: 12px !important

    .v-list--dense
      .v-list-item
        &__icon--text,
        &__icon:first-child
          margin-top: 10px

    .v-list-group--sub-group
      .v-list-item
        +ltr()
          padding-left: 8px

        +rtl()
          padding-right: 8px

      .v-list-group__header
        +ltr()
          padding-right: 0

        +rtl()
          padding-right: 0

        .v-list-item__icon--text
          margin-top: 19px
          order: 0

        .v-list-group__header__prepend-icon
          order: 2

          +ltr()
            margin-right: 8px

          +rtl()
            margin-left: 8px
</style>
