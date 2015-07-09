from django.shortcuts import render
from .models import Course, Section, Parent, Student

class ScheduledCourse(object):
    def __init__(self, name, description, sections):
        self.name = name
        self.description = description
        self.sections = sections

    def __str__(self, *args, **kwargs):
        return self.name + ' ' + self.description

class StudentStatus(object):
    def __init__(self, sections):
        self.sections = sections

class ParentStatus(object):
    def __init__(self, parent, studentStatuses):
        self.parent = parent
        self.studentStatuses = studentStatuses

def index(request):
    sections = Section.objects.order_by('start_time')
    scheduled_courses = set((s.course for s in sections))
    courses = (ScheduledCourse(c.name, c.description,
        [s for s in sections if s.course == c]) for c in Course.objects.order_by('name') if c in scheduled_courses)

    return render(request, 'app/courses.html', {'courses': courses})

def status(request):
    parents = Parent.objects.order_by('name')
    if parents:
        parent = parents[0]  # todo get the logged-in parent
        students = Student.objects.filter(parent=parent.id)
        student_status = StudentStatus([section for section in [student.sections for student in students]])
        parent_status = ParentStatus(parent, student_status)
    else:
        parent_status = None
    return render(request, 'app/status.html', {'parent_status': parent_status})
