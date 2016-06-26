from django.shortcuts import render
from django.contrib.auth.decorators import login_required
from .models import Course, Section, Parent


class ScheduledCourse(object):
    def __init__(self, name, url, sections):
        self.name = name
        self.url = url
        self.sections = sections

    def __str__(self, *args, **kwargs):
        return self.name + ' ' + self.description


def index(request):
    sections = Section.objects.order_by('start_time')
    scheduled_courses = set((s.course for s in sections))
    courses = (ScheduledCourse(c.name, c.url,
        [s for s in sections if s.course == c]) for c in Course.objects.order_by('name') if c in scheduled_courses)

    return render(request, 'app/courses.html', {'courses': courses})


@login_required
def status(request):
    parents = Parent.objects.order_by('name')
    return render(request, 'app/status.html', {'parents': parents})
