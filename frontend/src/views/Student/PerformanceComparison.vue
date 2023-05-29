<template>
  <v-container>
    <v-row class="text-right">
      <v-spacer></v-spacer>
      <v-col cols="6" md="2">
        <v-btn
            color="success"
            @click="downloadAudit( )"
        >
          Download
          <v-icon>
            mdi-download
          </v-icon>
        </v-btn>
      </v-col>
    </v-row>
    <v-expansion-panels v-model="panel" multiple>
      <v-expansion-panel>
        <v-expansion-panel-header>
          <span class="text-h4">
            Students
          </span>
        </v-expansion-panel-header>
        <v-expansion-panel-content>
          <v-row>
            <v-col
                cols="6"
                v-for="(student, index) in students" :key="index"
            >
              <base-material-card
                  color="info"
                  icon="mdi-account-details"
                  title="Student information"
              >
                <v-card-text>
                  <div class="display-2 font-weight-light">
                    {{student.name}}
                  </div>
                  <div class="display-2 font-weight-light">
                    {{student.id}}
                  </div>
                  <div class="text--primary font-weight-light">
                    {{student.major}}
                  </div>
                  <div class="text--primary font-weight-light">
                    Admission semester: {{student.admissionSemester}}
                  </div>
                  <div class="text--primary font-weight-light">
                    Current semester: {{student.currentSemester}}
                  </div>
                  <div class="text--primary">
                    <span class="font-weight-light">cGPA:</span> {{student.gpa}}
                  </div>
                  <div class="text--primary">
                    <span class="font-weight-light">Credits earned:</span> {{student.creditsEarnedTotal}}
                  </div>
                  <div class="text--primary">
                    <span class="font-weight-light">Courses failed:</span> {{reports[index].failedCourses.length}}
                  </div>
                  <div class="text--primary">
                    <span class="font-weight-light">Unmet degree requirements:</span> {{reports[index].unmappedRequirements.length}}
                  </div>
                  <router-link :to="'/student/' + student.id">
                    Details
                  </router-link>
                </v-card-text>
              </base-material-card>
            </v-col>
          </v-row>
        </v-expansion-panel-content>
      </v-expansion-panel>
      <v-expansion-panel>
        <v-expansion-panel-header><span class="text-h4">Comparison</span></v-expansion-panel-header>
        <v-expansion-panel-content>
          <v-row>
            <v-col>
              <base-material-chart-card
                  elevation="0"
                  :data="data"
                  :options="chartOptions"
                  color="success"
                  type="Line"
              >
                <div ref="legend" class="card-title font-weight-light mt-2 ml-2">
                  Legend
                </div>
              </base-material-chart-card>
            </v-col>
          </v-row>
        </v-expansion-panel-content>
      </v-expansion-panel>
    </v-expansion-panels>
  </v-container>
</template>

<script>
import {download, get, post} from "@/helpers/api";

export default {
  name: "ProblemComparison",
  props: [
      'ids',
      'curriculum'
  ],

  data() {
    return {
      data: {
        labels: [],
        series: []
      },
      students: [],
      reports: [],
      // panel: [0]
      panel: [0, 1],
      chartOptions: {
        high: 4,
        low: 0,
        lineSmooth: this.$chartist.Interpolation.none({
          fillHoles: false
        }),
        plugins: [
          this.$chartist.plugins.tooltip({ anchorToPoint: true }),
          this.$chartist.plugins.ctAxisTitle({
            axisY: {
              axisTitle: 'GPA',
              axisClass: 'gpa-axis-title',
              textAnchor: 'middle',
              offset: {
                x: 0,
                y: 20
              },
              flipTitle: true
            }
          }),
          this.$chartist.plugins.legend()
        ]
      }
    }
  },

  methods: {

    getTranscripts() {
      get(this, '/transcript/students/'+ this.ids, {}, response=> {

        this.students = response.data;
      });
    },

    buildReports() {

      for (let id of this.ids) {

        let data = {
          curriculumId: this.curriculum,
          studentId: id
        }

        post(this, '/report', data, response => {


          let report = response.data;
          this.reports.push(report);

          this.$store.dispatch('setSnackbar', {text: "Success"})
        }, error => {
          this.$store.dispatch('setSnackbar', {text: error, color: "error"});
        });

      }
    },

    getStudents() {
      let _this = this;
      console.log('/transcript/students/'+_this.ids);
      get(_this, '/transcript/students/'+_this.ids, {}, response=>{
        _this.students = response.data;
        _this.getGraph();
      });
    },
    getGraph() {
      let _this = this;
      get(_this, '/transcript/studentsGraph/'+_this.ids, {}, response=>{
        _this.data.labels = response.data.terms;
        _this.data.series = [];
        for (let id in response.data.graph){
          let student = response.data.graph[id];
          let name = "";
          this.students.forEach(student => {
            console.log(student.id + " == " + id);
            if (student.id == id)
              name = student.name;
          });
          let student_series = {name: name, data: []};
          response.data.terms.forEach(term => {

            let i = student.find(semester => {return semester["termName"] == term});

            if (i){
              student_series.data.push({x: i.termName, y: i.termGpa});
            }else{
              student_series.data.push({x: term, y: null})
            }
          });
          _this.data.series.push(student_series);
        }
      });
    },
    downloadAudit() {
      let _this = this;

      download(_this, '/report/batch/'+ _this.ids + '/export', '', response => {
      }, error => {
        _this.$store.dispatch('setSnackbar', {
          text: error,
          color: "error"
        })
      });
    },

  },

  created() {

    console.log(this.ids);
    this.getTranscripts();
    this.buildReports();
    this.getStudents();
    this.getGraph();
  }
}
</script>

<style scoped>

</style>